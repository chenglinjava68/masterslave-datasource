package cn.yxffcode.datasource.masterslave;

import cn.yxffcode.datasource.AbstractConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 实现读写分离的数据库连接,持有读连接和写连接
 * <p/>
 * 如果有写连接,则复用写连接,否则通过读写操作创建连接,总体策略上则表示在同一个连接上,
 * 如果先发生读,则使用从库,当有写发生后,读写都切到主库
 *
 * @author gaohang on 16/2/26.
 */
class MasterslaveConnection extends AbstractConnection {
  private static final Logger LOGGER = LoggerFactory.getLogger(MasterslaveConnection.class);

  private final MasterslaveDataSource groupDataSource;

  private Connection readConnection;
  private Connection writeConnection;
  private List<MasterslaveStatement> openedStatements = new ArrayList<>(1);

  public MasterslaveConnection(MasterslaveDataSource groupDataSource) {
    this.groupDataSource = groupDataSource;
  }

  public MasterslaveConnection(MasterslaveDataSource groupDataSource, String username,
                               String password) {
    this.groupDataSource = groupDataSource;
    this.username = username;
    this.password = password;
  }

  public boolean hasWriteConnection() {
    return writeConnection != null;
  }

  public void setAutoCommit(boolean autoCommit0) throws SQLException {
    checkClosed();
    if (this.isAutoCommit == autoCommit0) {
      return;
    }
    this.isAutoCommit = autoCommit0;
    if (this.writeConnection != null) {
      this.writeConnection.setAutoCommit(autoCommit0);
    }
  }

  public void commit() throws SQLException {
    checkClosed();
    if (isAutoCommit) {
      return;
    }

    if (writeConnection != null) {
      try {
        writeConnection.commit();
      } catch (SQLException e) {
        LOGGER.error(
            "Commit failed on write connection");
        throw e;
      }
    }
  }

  public void rollback() throws SQLException {
    checkClosed();
    if (isAutoCommit) {
      return;
    }

    //只有写需要回滚,读库没有更新,因此不需要回滚
    if (writeConnection != null) {
      try {
        writeConnection.rollback();
      } catch (SQLException e) {
        LOGGER.error(
            "Rollback failed on write connection:" + e.getMessage());
        throw e;
      }
    }
  }

  @Override public void close() throws SQLException {
    this.closed = true;
    SQLException last = null;
    //先关闭所有的statement
    for (MasterslaveStatement statement : openedStatements) {
      if (!statement.isClosed()) {
        try {
          statement.close();
        } catch (SQLException e) {
          last = e;
        }
      }
    }
    for (Connection connection : Arrays.asList(writeConnection, readConnection)) {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          last = e;
        }
      }
    }
    if (last != null) {
      throw last;
    }
  }

  public SQLWarning getWarnings() throws SQLException {
    checkClosed();
    if (readConnection != null) {
      return readConnection.getWarnings();
    } else if (writeConnection != null) {
      return writeConnection.getWarnings();
    } else {
      return null;
    }
  }

  public void clearWarnings() throws SQLException {
    checkClosed();
    if (readConnection != null) {
      readConnection.clearWarnings();
    }
    if (writeConnection != null) {
      writeConnection.clearWarnings();
    }
  }

  @Override public Statement createStatement(int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return new MasterslaveStatement(this, resultSetType, resultSetConcurrency, 0);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return new PreparedMasterslaveStatement(this, resultSetType, resultSetConcurrency, 0, sql);
  }

  public DatabaseMetaData getMetaData() throws SQLException {
    checkClosed();
    if (readConnection != null) {
      return readConnection.getMetaData();
    } else if (writeConnection != null) {
      return writeConnection.getMetaData();
    } else {
      readConnection = groupDataSource.readConnection(username, password);
      normalizeConnection();
      return readConnection.getMetaData();
    }
  }

  public String getCatalog() throws SQLException {
    throw new UnsupportedOperationException("getCatalog");
  }

  public void setCatalog(String catalog) throws SQLException {
    checkClosed();
    if (catalog.equals(this.catalog)) {
      return;
    }
    this.catalog = catalog;
    if (readConnection != null) {
      readConnection.setCatalog(catalog);
    }
    if (writeConnection != null) {
      writeConnection.setCatalog(catalog);
    }
  }

  @Override public Statement createStatement() throws SQLException {
    return new MasterslaveStatement(this);
  }

  @Override public PreparedStatement prepareStatement(String sql) throws SQLException {
    return new PreparedMasterslaveStatement(this, sql);
  }

  @Override public Statement createStatement(int resultSetType, int resultSetConcurrency,
                                             int resultSetHoldability) throws SQLException {
    return new MasterslaveStatement(this, resultSetType, resultSetConcurrency,
        resultSetHoldability);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
                                            int resultSetHoldability) throws SQLException {
    return new PreparedMasterslaveStatement(this, resultSetType, resultSetConcurrency,
        resultSetHoldability, sql);
  }

  @Override public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
      throws SQLException {
    return new PreparedMasterslaveStatement(this, autoGeneratedKeys, sql);
  }

  @Override public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
      throws SQLException {
    return new PreparedMasterslaveStatement(this, sql, columnIndexes);
  }

  @Override public PreparedStatement prepareStatement(String sql, String[] columnNames)
      throws SQLException {
    return new PreparedMasterslaveStatement(this, sql, columnNames);
  }

  Connection targetConnection(boolean read) throws SQLException {
    if (writeConnection != null) {
      return writeConnection;
    }
    if (read && readConnection == null) {
      readConnection = groupDataSource.readConnection(username, password);
    } else if (!read && writeConnection == null) {
      writeConnection = groupDataSource.writeConnection(username, password);
      normalizeConnection();
    }
    return read ? readConnection : writeConnection;
  }

  private void normalizeConnection() throws SQLException {
    if (!isAutoCommit) {
      writeConnection.setAutoCommit(false);
    }
    if (transactionIsolation != -1) {
      writeConnection.setTransactionIsolation(transactionIsolation);
    }
    if (catalog != null) {
      writeConnection.setCatalog(catalog);
    }
  }

}
