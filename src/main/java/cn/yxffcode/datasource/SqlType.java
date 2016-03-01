package cn.yxffcode.datasource;

import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * SQL类型。
 *
 * @author gaohang
 */
public enum SqlType {
  SELECT(0), INSERT(1), UPDATE(2), DELETE(3), SELECT_FOR_UPDATE(4), REPLACE(5), TRUNCATE(6),
  CREATE(7), DROP(8), LOAD(9), SHOW(10), ALTER(111), DEFAULT_SQL_TYPE(-100);

  private static final Pattern SELECT_FOR_UPDATE_PATTERN =
      Pattern.compile("^select\\s+.*\\s+for\\s+update.*$", Pattern.CASE_INSENSITIVE);
  private final int i;

  SqlType(int i) {
    this.i = i;
  }

  public boolean isRead() {
    switch (this) {
      case SELECT:
      case SHOW:
      case LOAD:
        return true;
      default:
        return false;
    }
  }

  public static SqlType atomOf(String sql) throws SQLException {
    SqlType sqlType;
    String noCommentsSql =
        StringUtils.stripComments(sql, "'\"", "'\"", true, false, true, true).trim();

    if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "select")) {
      if (SELECT_FOR_UPDATE_PATTERN.matcher(noCommentsSql).matches()) {
        sqlType = SqlType.SELECT_FOR_UPDATE;
      } else {
        sqlType = SqlType.SELECT;
      }
    } else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "show")) {
      sqlType = SqlType.SHOW;
    } else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "insert")) {
      sqlType = SqlType.INSERT;
    } else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "update")) {
      sqlType = SqlType.UPDATE;
    } else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "delete")) {
      sqlType = SqlType.DELETE;
    } else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "replace")) {
      sqlType = SqlType.REPLACE;
    } else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "truncate")) {
      sqlType = SqlType.TRUNCATE;
    } else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "create")) {
      sqlType = SqlType.CREATE;
    } else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "drop")) {
      sqlType = SqlType.DROP;
    } else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "load")) {
      sqlType = SqlType.LOAD;
    } else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "alter")) {
      sqlType = SqlType.ALTER;
    } else {
      throw new SQLException(
          "only select, insert, update, delete,replace,truncate sql is supported");
    }
    return sqlType;
  }

  public int value() {
    return this.i;
  }
}
