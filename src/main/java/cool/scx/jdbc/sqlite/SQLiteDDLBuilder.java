package cool.scx.jdbc.sqlite;

import cool.scx.jdbc.JDBCType;
import cool.scx.jdbc.dialect.DDLBuilder;
import cool.scx.jdbc.mapping.Column;

import java.util.ArrayList;
import java.util.List;

import static cool.scx.common.util.StringUtils.notBlank;

/**
 * SQLiteDDLBuilder
 *
 * @author scx567888
 * @version 0.0.1
 */
public class SQLiteDDLBuilder implements DDLBuilder {

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

}
