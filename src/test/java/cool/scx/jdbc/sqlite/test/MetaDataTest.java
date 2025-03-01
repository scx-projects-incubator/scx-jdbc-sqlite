package cool.scx.jdbc.sqlite.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import cool.scx.jdbc.meta_data.DataSourceMetaData;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

public class MetaDataTest {

    public static void main(String[] args) throws SQLException, JsonProcessingException {
        test1();
    }

    @Test
    public static void test1() throws SQLException, JsonProcessingException {
        test1_1(SQLRunnerForSQLiteTest.dataSource);
        test1_2(SQLRunnerForSQLiteTest.dataSource);
    }

    public static void test1_1(DataSource dataSource) throws SQLException, JsonProcessingException {
        System.out.println("完全内省 !!!");
        try (var con = dataSource.getConnection()) {
            var dataSourceMetaData = new DataSourceMetaData().refreshCatalogs(con, true);
            for (var catalog : dataSourceMetaData.catalogs()) {
                System.out.println(catalog.name() + " : ");
                System.out.println("{");
                for (var schema : catalog.schemas()) {
                    System.out.println("    " + schema.name() + " : ");
                    System.out.println("    {");
                    for (var table : schema.tables()) {
                        System.out.println("        " + table.name() + " : ");
                        System.out.println("        {");
                        for (var column : table.columns()) {
                            System.out.println("            " + column.name() + " " + column.dataType().name() + "(" + column.dataType().length() + ")" + (column.notNull() ? " NOT NULL" : " NULL") + (column.autoIncrement() ? " AUTOINCREMENT" : "") + " DEFAULT : " + column.defaultValue() + ", REMARKS : " + column.comment());
                        }
                        for (var primaryKey : table.keys()) {
                            System.out.println("            " + primaryKey.columnName() + " primaryKeys ");
                        }
                        System.out.println("        }");
                    }
                    System.out.println("    }");
                }
                System.out.println("}");
            }
        }

    }


    public static void test1_2(DataSource dataSource) throws SQLException, JsonProcessingException {
        System.out.println("按需内省 !!!");
        try (var con = dataSource.getConnection()) {
            var dataSourceMetaData = new DataSourceMetaData().refreshCatalogs(con);
            for (var catalog : dataSourceMetaData.catalogs()) {
                System.out.println(catalog.name() + " : ");
                System.out.println("{");
                for (var schema : catalog.refreshSchemas(con).schemas()) {
                    System.out.println("    " + schema.name() + " : ");
                    System.out.println("    {");
                    for (var table : schema.refreshTables(con).tables()) {
                        System.out.println("        " + table.name() + " : ");
                        System.out.println("        {");
                        for (var column : table.refreshColumns(con).columns()) {
                            System.out.println("            " + column.name() + " " + column.dataType().name() + "(" + column.dataType().length() + ")" + (column.notNull() ? " NOT NULL" : " NULL") + (column.autoIncrement() ? " AUTOINCREMENT" : "") + " DEFAULT : " + column.defaultValue() + ", REMARKS : " + column.comment());
                        }
                        for (var primaryKey : table.keys()) {
                            System.out.println("            " + primaryKey.columnName() + " primaryKeys ");
                        }
                        System.out.println("        }");
                    }
                    System.out.println("    }");
                }
                System.out.println("}");
            }
        }
    }

}
