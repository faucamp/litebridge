package org.litebridge.db.spi;

import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateResult;

import java.sql.SQLException;
import java.util.List;

public interface DatabaseProvider {

    TableMetaData getTableMetaData(Table table) throws SQLException;

    InsertResult insert(Insert insert) throws SQLException;

    UpdateResult update(Update update) throws SQLException;

    List<Row> select(Select select) throws SQLException;

    String toSql(Select select);

    TypeConverter getTypeConverter();
}
