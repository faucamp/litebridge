package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Delete;

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
