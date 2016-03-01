package cn.yxffcode.datasource.masterslave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 同时对多个数据源做检测
 *
 * @author gaohang on 16/3/1.
 */
abstract class ConcurrentDataSourceHealthChecker {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ConcurrentDataSourceHealthChecker.class);

  private ScheduledExecutorService scheduler;
  private long initialDelay;
  private long monitorPeriod;
  private int recheckTimes;
  private int threadCount;
  private int recheckInterval;
  private String detectSql;

  public void check(DataSource... dataSources) {
    checkNotNull(dataSources);
    ensureInitScheduler();

    //start to check
    for (final DataSource dataSource : dataSources) {
      if (dataSource == null) {
        continue;
      }
      scheduler.scheduleWithFixedDelay(new Runnable() {
        @Override public void run() {
          //check DataSource
          for (int i = 0; i < recheckTimes; i++) {
            Connection connection = null;
            PreparedStatement statement = null;
            try {
              connection = dataSource.getConnection();
              statement = connection.prepareStatement(detectSql);
              statement.execute();
              onAvailable(dataSource);
              break;
            } catch (SQLException e) {
              LOGGER.error("healthcheck failed:{}", dataSource, e);
              if (i < recheckTimes - 1) {
                //recheck
                try {
                  TimeUnit.MILLISECONDS.sleep(recheckInterval);
                } catch (InterruptedException e1) {
                  LOGGER.error("healthcheck sleep failed:{}", detectSql, e1);
                }
              } else {
                //检查失败
                onUnavailable(dataSource);
              }
            } finally {
              if (statement != null) {
                try {
                  statement.close();
                } catch (SQLException e) {
                  LOGGER.error("close statement failed:{}", detectSql, e);
                }
              }
              if (connection != null) {
                try {
                  connection.close();
                } catch (SQLException e) {
                  LOGGER.error("close connection failed:{}", detectSql, e);
                }
              }
            }
          }
        }
      }, initialDelay, monitorPeriod, TimeUnit.MILLISECONDS);
    }
  }

  protected abstract void onAvailable(DataSource dataSource);

  protected abstract void onUnavailable(DataSource dataSource);

  private void ensureInitScheduler() {
    if (scheduler == null) {
      synchronized (this) {
        if (scheduler == null) {
          scheduler = Executors.newScheduledThreadPool(threadCount, new ThreadFactory() {
            private AtomicInteger threadCounter = new AtomicInteger();

            @Override public Thread newThread(Runnable r) {
              Thread t = new Thread(r);
              t.setName("DataSource-health-check-thread-" + threadCounter.getAndIncrement());
              t.setDaemon(true);
              if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
              }
              return t;
            }
          });
        }
      }
    }
  }

  public long getInitialDelay() {
    return initialDelay;
  }

  public void setInitialDelay(long initialDelay) {
    this.initialDelay = initialDelay;
  }

  public long getMonitorPeriod() {
    return monitorPeriod;
  }

  public void setMonitorPeriod(long monitorPeriod) {
    this.monitorPeriod = monitorPeriod;
  }

  public int getRecheckTimes() {
    return recheckTimes;
  }

  public void setRecheckTimes(int recheckTimes) {
    this.recheckTimes = recheckTimes;
  }

  public int getThreadCount() {
    return threadCount;
  }

  public void setThreadCount(int threadCount) {
    this.threadCount = threadCount;
  }

  public int getRecheckInterval() {
    return recheckInterval;
  }

  public void setRecheckInterval(int recheckInterval) {
    this.recheckInterval = recheckInterval;
  }

  public String getDetectSql() {
    return detectSql;
  }

  public void setDetectSql(String detectSql) {
    this.detectSql = detectSql;
  }
}
