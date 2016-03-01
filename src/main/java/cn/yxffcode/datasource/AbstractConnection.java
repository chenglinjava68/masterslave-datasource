package cn.yxffcode.datasource;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author gaohang on 16/2/28.
 */
public abstract class AbstractConnection implements Connection {
  protected boolean closed;

  // FIXME: 16/2/29 username & password 没起到作用
  protected String username;
  protected String password;
  protected boolean isAutoCommit = true; // jdbc规范，新连接为true
  protected int transactionIsolation = -1;
  protected volatile String catalog;

  public boolean getAutoCommit() throws SQLException {
    checkClosed();
    return isAutoCommit;
  }

  @Override public boolean isClosed() throws SQLException {
    return closed;
  }

  public int getTransactionIsolation() throws SQLException {
    checkClosed();
    return transactionIsolation;
  }

  public void setTransactionIsolation(int transactionIsolation) throws SQLException {
    checkClosed();
    this.transactionIsolation = transactionIsolation;
  }

  protected void checkClosed() throws SQLException {
    if (closed) {
      throw new SQLException("No operations allowed after connection closed.");
    }
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    throw new UnsupportedOperationException(sql);
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                       int resultSetHoldability) throws SQLException {
    throw new UnsupportedOperationException("callable statement:" + sql);
  }

  public int getHoldability() throws SQLException {
    return ResultSet.CLOSE_CURSORS_AT_COMMIT;
  }

  public void rollback(Savepoint savepoint) throws SQLException {
    throw new UnsupportedOperationException("rollback");
  }

  public Savepoint setSavepoint() throws SQLException {
    throw new UnsupportedOperationException("setSavepoint");
  }

  public Savepoint setSavepoint(String name) throws SQLException {
    throw new UnsupportedOperationException("setSavepoint");
  }

  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    throw new UnsupportedOperationException("releaseSavepoint");
  }

  public void setHoldability(int holdability) throws SQLException {
    /*
     * mysql默认在5.x的jdbc driver里面也没有实现holdability 。
		 * 所以默认都是.CLOSE_CURSORS_AT_COMMIT 为了简化起见，我们也就只实现close这种
		 */
    throw new UnsupportedOperationException("setHoldability");
  }

  public Map<String, Class<?>> getTypeMap() throws SQLException {
    throw new UnsupportedOperationException("getTypeMap");
  }

  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    throw new UnsupportedOperationException("setTypeMap");
  }

  @Override public CallableStatement prepareCall(String sql) throws SQLException {
    throw new UnsupportedOperationException("callable statement:" + sql);
  }

  public String nativeSQL(String sql) throws SQLException {
    throw new UnsupportedOperationException("nativeSQL");
  }

  /**
   * 保持可读可写
   */
  public boolean isReadOnly() throws SQLException {
    return false;
  }

  /**
   * 不做任何事情
   */
  public void setReadOnly(boolean readOnly) throws SQLException {
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return this.getClass().isAssignableFrom(iface);
  }

  @SuppressWarnings("unchecked")
  public <T> T unwrap(Class<T> iface) throws SQLException {
    try {
      return (T) this;
    } catch (Exception e) {
      throw new SQLException(e);
    }
  }

  public Clob createClob() throws SQLException {
    throw new SQLException("not support exception");
  }

  public Blob createBlob() throws SQLException {
    throw new SQLException("not support exception");
  }

  public NClob createNClob() throws SQLException {
    throw new SQLException("not support exception");
  }

  public SQLXML createSQLXML() throws SQLException {
    throw new SQLException("not support exception");
  }

  public boolean isValid(int timeout) throws SQLException {
    throw new SQLException("not support exception");
  }

  public void setClientInfo(String name, String value) throws SQLClientInfoException {
    throw new RuntimeException("not support exception");
  }

  public String getClientInfo(String name) throws SQLException {
    throw new SQLException("not support exception");
  }

  public Properties getClientInfo() throws SQLException {
    throw new SQLException("not support exception");
  }

  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    throw new RuntimeException("not support exception");
  }

  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    throw new SQLException("not support exception");
  }

  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    throw new SQLException("not support exception");
  }

  public String getSchema() throws SQLException {
    throw new SQLException("not support exception");
  }

  public void setSchema(String schema) throws SQLException {
    throw new SQLException("not support exception");
  }

  public void abort(Executor executor) throws SQLException {
    throw new SQLException("not support exception");
  }

  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    throw new SQLException("not support exception");
  }

  public int getNetworkTimeout() throws SQLException {
    throw new SQLException("not support exception");
  }

}
