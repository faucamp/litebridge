package org.litebridgedb.db.spi.impl.sql;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.sql.BindValue;
import org.litebridgedb.db.spi.sql.PreparedSql;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.litebridgedb.db.spi.update.Delete;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class DeleteSqlGenerator extends AbstractSqlGenerator {

    public DeleteSqlGenerator(final TypeConverter typeConverter,
                              final ColumnIdentifierGenerator columnIdentifierGenerator,
                              final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(typeConverter, columnIdentifierGenerator, ensureTableMetaData);
    }

    public PreparedSql prepareSql(final Delete delete, final ConnectionProvider connectionProvider) {
        final StringBuilder sql = appendTable(new StringBuilder("DELETE FROM "), delete.table());
        final List<BindValue> bindValues = new ArrayList<>();

        if (!delete.where().isEmpty()) {
            sql.append(" WHERE ");
            appendConditionsAndSubgroups(sql, delete.where(), bindValues, delete, connectionProvider);
        }

        return new PreparedSql(sql.toString(), bindValues);
    }
}
