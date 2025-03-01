package cool.scx.jdbc.sqlite;

import cool.scx.jdbc.JDBCHelper;
import cool.scx.jdbc.JDBCType;
import cool.scx.jdbc.dialect.DDLBuilder;
import cool.scx.jdbc.dialect.Dialect;
import cool.scx.jdbc.sqlite.type_handler.SQLiteLocalDateTimeTypeHandler;
import org.sqlite.JDBC;
import org.sqlite.SQLiteDataSource;
import org.sqlite.core.CorePreparedStatement;
import org.sqlite.core.CoreStatement;
import org.sqlite.jdbc4.JDBC4PreparedStatement;

import javax.sql.DataSource;
import java.lang.System.Logger;
import java.lang.reflect.Field;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

/**
 * SQLiteDialect
 *
 * @author scx567888
 * @version 0.0.1
 * @see <a href="https://www.sqlite.org/lang_createtable.html">https://www.sqlite.org/lang_createtable.html</a>
 */
public class SQLiteDialect extends Dialect {

    public static final Logger logger = System.getLogger(SQLiteDialect.class.getName());
    static final Field CoreStatement_sql = initCoreStatement_sql();
    static final Field CoreStatement_batch = initCoreStatement_batch();
    static final Field CorePreparedStatement_batchQueryCount = initCorePreparedStatement_batchQueryCount();
    private static final SQLiteDDLBuilder SQLite_DDL_BUILDER = new SQLiteDDLBuilder();
    private static final org.sqlite.JDBC DRIVER = initDRIVER();

    public SQLiteDialect() {
        // 注册自定义的 TypeHandler       
        this.typeHandlerSelector.registerTypeHandler(LocalDateTime.class, new SQLiteLocalDateTimeTypeHandler());
    }

    private static Field initCoreStatement_sql() {
        try {
            var f = CoreStatement.class.getDeclaredField("sql");
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field initCoreStatement_batch() {
        try {
            var f = CoreStatement.class.getDeclaredField("batch");
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field initCorePreparedStatement_batchQueryCount() {
        try {
            var f = CorePreparedStatement.class.getDeclaredField("batchQueryCount");
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static JDBC initDRIVER() {
        try {
            return new org.sqlite.JDBC();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canHandle(String url) {
        return DRIVER.acceptsURL(url);
    }

    @Override
    public boolean canHandle(DataSource dataSource) {
        try {
            return dataSource instanceof SQLiteDataSource || dataSource.isWrapperFor(SQLiteDataSource.class);
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean canHandle(Driver driver) {
        return driver instanceof org.sqlite.JDBC;
    }

    @Override
    public String getFinalSQL(Statement statement) {
        CorePreparedStatement corePreparedStatement;
        try {
            corePreparedStatement = statement.unwrap(JDBC4PreparedStatement.class);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        final String sql;
        final Object[] batch;
        final int batchQueryCount;
        try {
            sql = (String) CoreStatement_sql.get(corePreparedStatement);
            batch = (Object[]) CoreStatement_batch.get(corePreparedStatement);
            batchQueryCount = (int) CorePreparedStatement_batchQueryCount.get(corePreparedStatement);
        } catch (IllegalAccessException e) {
            return null;
        }
        var finalSQL = JDBCHelper.getSqlWithValues(sql, batch);
        return batchQueryCount > 1 ? finalSQL + "... 额外的 " + (batchQueryCount - 1) + " 项" : finalSQL;
    }

    @Override
    public DDLBuilder ddlBuilder() {
        return SQLite_DDL_BUILDER;
    }

    @Override
    public DataSource createDataSource(String url, String username, String password, String[] parameters) {
        var sqLiteDataSource = new SQLiteDataSource();
        sqLiteDataSource.setUrl(url);
        return sqLiteDataSource;
    }

    @Override
    public JDBCType dialectDataTypeToJDBCType(String dialectDataType) {
        return SQLiteDialectHelper.dialectDataTypeToJDBCType(dialectDataType);
    }

    @Override
    public String jdbcTypeToDialectDataType(JDBCType jdbcType) {
        return SQLiteDialectHelper.jdbcTypeToDialectDataType(jdbcType);
    }

}
