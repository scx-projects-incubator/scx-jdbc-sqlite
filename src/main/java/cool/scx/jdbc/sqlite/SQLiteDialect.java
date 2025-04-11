package cool.scx.jdbc.sqlite;

import cool.scx.jdbc.JDBCHelper;
import cool.scx.jdbc.JDBCType;
import cool.scx.jdbc.dialect.Dialect;
import cool.scx.jdbc.mapping.Column;
import cool.scx.jdbc.sqlite.type_handler.SQLiteLocalDateTimeTypeHandler;
import cool.scx.jdbc.type_handler.TypeHandler;
import cool.scx.jdbc.type_handler.TypeHandlerSelector;
import org.sqlite.JDBC;
import org.sqlite.SQLiteDataSource;
import org.sqlite.core.CorePreparedStatement;
import org.sqlite.core.CoreStatement;
import org.sqlite.jdbc4.JDBC4PreparedStatement;

import javax.sql.DataSource;
import java.lang.System.Logger;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cool.scx.common.util.StringUtils.notBlank;

/**
 * SQLiteDialect
 *
 * @author scx567888
 * @version 0.0.1
 * @see <a href="https://www.sqlite.org/lang_createtable.html">https://www.sqlite.org/lang_createtable.html</a>
 */
public class SQLiteDialect implements Dialect {

    public static final Logger logger = System.getLogger(SQLiteDialect.class.getName());
    static final Field CoreStatement_sql = initCoreStatement_sql();
    static final Field CoreStatement_batch = initCoreStatement_batch();
    static final Field CorePreparedStatement_batchQueryCount = initCorePreparedStatement_batchQueryCount();
    private static final org.sqlite.JDBC DRIVER = initDRIVER();
    private final TypeHandlerSelector typeHandlerSelector;

    public SQLiteDialect() {
        // 注册自定义的 TypeHandler       
        this.typeHandlerSelector = new TypeHandlerSelector();
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
    public DataSource createDataSource(String url, String username, String password, String[] parameters) {
        var sqLiteDataSource = new SQLiteDataSource();
        sqLiteDataSource.setUrl(url);
        return sqLiteDataSource;
    }

    @Override
    public <T> TypeHandler<T> findTypeHandler(Type type) {
        return typeHandlerSelector.findTypeHandler(type);
    }

    @Override
    public JDBCType dialectDataTypeToJDBCType(String dialectDataType) {
        return SQLiteDialectHelper.dialectDataTypeToJDBCType(dialectDataType);
    }

    @Override
    public String jdbcTypeToDialectDataType(JDBCType jdbcType) {
        return SQLiteDialectHelper.jdbcTypeToDialectDataType(jdbcType);
    }

    @Override
    public String getDataTypeNameByJDBCType(JDBCType dataType) {
        return SQLiteDialectHelper.jdbcTypeToDialectDataType(dataType);
    }

    @Override
    public String getDataTypeDefinitionByName(String dataType, Integer length) {
        //SQLite 的类型无需长度
        return dataType;
    }

    /**
     * 当前列对象通常的 DDL 如设置 字段名 类型 是否可以为空 默认值等 (建表语句片段 , 需和 specialDDL 一起使用才完整)
     */
    @Override
    public List<String> getColumnConstraint(Column column) {
        var list = new ArrayList<String>();
        if (column.primary() && column.autoIncrement()) {
            list.add("PRIMARY KEY AUTOINCREMENT");
        }
        list.add(column.notNull() || column.primary() ? "NOT NULL" : "NULL");
        if (column.unique()) {
            list.add("UNIQUE");
        }
        if (notBlank(column.defaultValue())) {
            list.add("DEFAULT " + column.defaultValue());
        }
        return list;
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return '"' + identifier + '"';
    }
    
}
