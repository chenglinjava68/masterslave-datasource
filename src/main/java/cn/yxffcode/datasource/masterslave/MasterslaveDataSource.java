package cn.yxffcode.datasource.masterslave;

import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * 支持读写分离的DataSource,适用于一主一从场景
 *
 * @author gaohang on 16/2/26.
 */
class MasterslaveDataSource extends AbstractDataSource {

  private DataSource master;
  private DataSource slave;
  private boolean slaveWritable;

  private volatile boolean masterAvailable = true;
  private volatile boolean slaveAvailable = true;

  private Connection readConnection() throws SQLException {
    if (slave == null || !slaveAvailable) {
      return writeConnection();
    }
    return slave.getConnection();
  }

  Connection readConnection(String username, String password) throws SQLException {
    if (isNullOrEmpty(username) && isNullOrEmpty(password)) {
      return readConnection();
    }
    if (slave == null || !slaveAvailable) {
      return writeConnection(username, password);
    }
    return slave.getConnection(username, password);
  }

  private Connection writeConnection() throws SQLException {
    if (!masterAvailable && slaveWritable && slaveAvailable) {
      return slave.getConnection();
    }
    return master.getConnection();
  }

  Connection writeConnection(String username, String password) throws SQLException {
    if (isNullOrEmpty(username) && isNullOrEmpty(password)) {
      return writeConnection();
    }
    if (!masterAvailable && slaveWritable && slaveAvailable) {
      return slave.getConnection();
    }
    return master.getConnection(username, password);
  }

  @Override public Connection getConnection() throws SQLException {
    return new MasterslaveConnection(this);
  }

  @Override public Connection getConnection(String username, String password) throws SQLException {
    return new MasterslaveConnection(this, username, password);
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

  public boolean isMasterAvailable() {
    return masterAvailable;
  }

  public void setMasterAvailable(boolean masterAvailable) {
    this.masterAvailable = masterAvailable;
  }

  public boolean isSlaveAvailable() {
    return slaveAvailable;
  }

  public void setSlaveAvailable(boolean slaveAvailable) {
    this.slaveAvailable = slaveAvailable;
  }

  public boolean isSlaveWritable() {
    return slaveWritable;
  }

  public void setSlaveWritable(boolean slaveWritable) {
    this.slaveWritable = slaveWritable;
  }
}
