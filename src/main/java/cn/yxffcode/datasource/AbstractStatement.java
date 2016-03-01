package cn.yxffcode.datasource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author gaohang on 16/2/28.
 */
public abstract class AbstractStatement implements Statement {
  protected int resultSetHoldability;
  protected int resultSetType;
  protected int resultSetConcurrency;

  protected boolean closed;

  /**
   * 超时时间，如果超时时间不为0。那么超时应该被set到真正的statement中。
   */
  protected int queryTimeout = 0;

  /**
   * 执行查询后的结果集
   */
  protected ResultSet currentResultSet;
  /**
   * 执行更新后的更行行数
   */
  protected int updateCount;

  protected int fetchSize;
  protected int maxRows;

  public int getMaxRows() throws SQLException {
    return this.maxRows;
  }

  public void setMaxRows(int maxRows) throws SQLException {
    this.maxRows = maxRows;
  }

  public int getFetchSize() throws SQLException {
    return this.fetchSize;
  }

  public void setFetchSize(int fetchSize) throws SQLException {
    this.fetchSize = fetchSize;
  }

  public boolean getMoreResults() throws SQLException {
    return false;
  }

  public int getQueryTimeout() throws SQLException {
    return queryTimeout;
  }

  public void setQueryTimeout(int queryTimeout) throws SQLException {
    this.queryTimeout = queryTimeout;
  }

  public ResultSet getResultSet() throws SQLException {
    return currentResultSet;
  }

  public int getUpdateCount() throws SQLException {
    return updateCount;
  }

  public int getResultSetConcurrency() throws SQLException {
    return resultSetConcurrency;
  }

  public void setResultSetConcurrency(int resultSetConcurrency) {
    this.resultSetConcurrency = resultSetConcurrency;
  }

  public int getResultSetType() throws SQLException {
    return resultSetType;
  }

  public void setResultSetType(int resultSetType) {
    this.resultSetType = resultSetType;
  }

  public int getResultSetHoldability() throws SQLException {
    return resultSetHoldability;
  }

  public void setResultSetHoldability(int resultSetHoldability) {
    this.resultSetHoldability = resultSetHoldability;
  }

  /* ========================================================================
   * 以下为不支持的方法
   * ======================================================================*/
  public int getFetchDirection() throws SQLException {
    throw new UnsupportedOperationException("getFetchDirection");
  }

  public void setFetchDirection(int fetchDirection) throws SQLException {
    throw new UnsupportedOperationException("setFetchDirection");
  }

  public int getMaxFieldSize() throws SQLException {
    throw new UnsupportedOperationException("getMaxFieldSize");
  }

  public void setMaxFieldSize(int maxFieldSize) throws SQLException {
    throw new UnsupportedOperationException("setMaxFieldSize");
  }

  public void setCursorName(String cursorName) throws SQLException {
    throw new UnsupportedOperationException("setCursorName");
  }

  public void setEscapeProcessing(boolean escapeProcessing) throws SQLException {
    throw new UnsupportedOperationException("setEscapeProcessing");
  }

  public boolean getMoreResults(int current) throws SQLException {
    throw new UnsupportedOperationException("getMoreResults");
  }

  public void cancel() throws SQLException {
    throw new UnsupportedOperationException("cancel");
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

  public boolean isClosed() throws SQLException {
    throw new SQLException("not support exception");
  }

  public boolean isPoolable() throws SQLException {
    throw new SQLException("not support exception");
  }

  public void setPoolable(boolean poolable) throws SQLException {
    throw new SQLException("not support exception");
  }

  public void closeOnCompletion() throws SQLException {
    throw new SQLException("not support exception");
  }

  public boolean isCloseOnCompletion() throws SQLException {
    throw new SQLException("not support exception");
  }

  protected void checkClosed() throws SQLException {
    if (closed) {
      throw new SQLException("No operations allowed after statement closed.");
    }
  }

}
