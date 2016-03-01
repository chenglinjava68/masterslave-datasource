package cn.yxffcode.datasource.masterslave;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author gaohang on 16/3/1.
 */
public class MasterslaveDataSourceFactoryBean implements FactoryBean<DataSource>, InitializingBean {

  private MasterslaveDataSource masterslaveDataSource;

  private DataSource master;
  private DataSource slave;
  private long initialDelay = 15 * 1000;
  private long monitorPeriod = 15 * 1000;
  private int recheckTimes = 3;
  private int threadCount = 2;
  private int recheckInterval = 1000;
  private String detectSql = "select 1";
  private boolean healthcheck;
  private boolean slaveWritable;

  @Override public DataSource getObject() throws Exception {
    masterslaveDataSource = new MasterslaveDataSource();
    masterslaveDataSource.setMaster(master);
    if (slave == null) {
      masterslaveDataSource.setSlaveAvailable(false);
    } else {
      masterslaveDataSource.setSlave(slave);
      masterslaveDataSource.setSlaveWritable(slaveWritable);
    }
    if (healthcheck) {
      ConcurrentDataSourceHealthChecker healthchecker =
          new ConcurrentDataSourceHealthChecker() {
            @Override protected void onAvailable(DataSource dataSource) {
              if (dataSource == master) {
                masterslaveDataSource.setMasterAvailable(true);
              } else {
                masterslaveDataSource.setSlaveAvailable(true);
              }
            }

            @Override protected void onUnavailable(DataSource dataSource) {
              if (dataSource == master) {
                masterslaveDataSource.setMasterAvailable(false);
              } else {
                masterslaveDataSource.setSlaveAvailable(false);
              }
            }
          };
      healthchecker.setInitialDelay(initialDelay);
      healthchecker.setMonitorPeriod(monitorPeriod);
      healthchecker.setRecheckTimes(recheckTimes);
      healthchecker.setRecheckInterval(recheckInterval);
      healthchecker.setDetectSql(detectSql);
      healthchecker.check(master, slave);
    }
    return masterslaveDataSource;
  }

  @Override public Class<?> getObjectType() {
    return DataSource.class;
  }

  @Override public boolean isSingleton() {
    return true;
  }

  public DataSource getMaster() {
    return master;
  }

  public void setMaster(DataSource master) {
    this.master = master;
  }

  public DataSource getSlave() {
    return slave;
  }

  public void setSlave(DataSource slave) {
    this.slave = slave;
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

  public boolean isHealthcheck() {
    return healthcheck;
  }

  public void setHealthcheck(boolean healthcheck) {
    this.healthcheck = healthcheck;
  }

  public boolean isSlaveWritable() {
    return slaveWritable;
  }

  public void setSlaveWritable(boolean slaveWritable) {
    this.slaveWritable = slaveWritable;
  }

  @Override public void afterPropertiesSet() throws Exception {
    checkNotNull(master);
  }
}
