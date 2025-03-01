package cool.scx.jdbc.sqlite.type_handler;

import cool.scx.jdbc.type_handler.time.LocalDateTimeTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 当前驱动不支持 LocalDateTime, 尝试使用 Timestamp 进行转换 !!!
 *
 * @author scx567888
 * @version 0.0.1
 */
public class SQLiteLocalDateTimeTypeHandler extends LocalDateTimeTypeHandler {

    @Override
    public void setObject(PreparedStatement ps, int i, LocalDateTime parameter) throws SQLException {
        ps.setTimestamp(i, Timestamp.valueOf(parameter));
    }

    @Override
    public LocalDateTime getObject(ResultSet rs, int index) throws SQLException {
        var timestamp = rs.getTimestamp(index);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

}
