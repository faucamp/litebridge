package org.litebridgedb.db.oracle.sql;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridgedb.db.spi.query.Limit;
import org.litebridgedb.db.spi.tx.ConnectionProvider;

import java.util.function.BiFunction;

public class OracleSelectSqlGenerator extends SelectSqlGenerator {

    public OracleSelectSqlGenerator(final TypeConverter typeConverter, final ColumnIdentifierGenerator columnIdentifierGenerator, final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(typeConverter, columnIdentifierGenerator, ensureTableMetaData);
    }

    @Override
    protected void appendLimitClause(final Limit limit, final StringBuilder sql) {
        limit.offset().ifPresent(offset -> sql.append(" OFFSET ").append(offset).append(" ROWS"));
        limit.limit().ifPresent(limitVal -> sql.append(" FETCH FIRST ").append(limitVal).append(" ROWS ONLY"));
    }
}
