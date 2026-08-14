package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.Update;

import java.util.function.BiFunction;

public class MergeSqlGenerator extends AbstractSqlGenerator {

    private final InsertSqlGenerator insertSqlGenerator;
    private final UpdateSqlGenerator updateSqlGenerator;
    private final DeleteSqlGenerator deleteSqlGenerator;

    public MergeSqlGenerator(final TypeConverter typeConverter,
                             final ColumnIdentifierGenerator columnIdentifierGenerator,
                             final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData,
                             final InsertSqlGenerator insertSqlGenerator,
                             final UpdateSqlGenerator updateSqlGenerator,
                             final DeleteSqlGenerator deleteSqlGenerator) {
        super(typeConverter, columnIdentifierGenerator, ensureTableMetaData);
        this.insertSqlGenerator = insertSqlGenerator;
        this.updateSqlGenerator = updateSqlGenerator;
        this.deleteSqlGenerator = deleteSqlGenerator;
    }

    public String prepareSql(final Merge merge, final ConnectionProvider connectionProvider) {
        final StringBuilder sql = appendTable(new StringBuilder("MERGE INTO "), merge.table());
        sql.append(" USING (");

        if (merge.usingTable() != null) {
            appendTable(sql, merge.usingTable());
        }

        sql.append(") ON ");
        appendConditionsAndSubgroups(sql, merge.on(), merge, connectionProvider);

        for (Merge.WhenMatched whenMatched : merge.whenMatched()) {
            sql.append(" WHEN MATCHED THEN ");

            if (whenMatched.operation() instanceof Update update) {
                sql.append(updateSqlGenerator.prepareSql(update, true, connectionProvider));
            } else if (whenMatched.operation() instanceof Delete delete) {
                sql.append(deleteSqlGenerator.prepareSql(delete, connectionProvider));
            } else {
                throw new IllegalArgumentException("Unsupported operation type: " + whenMatched.operation().getClass().getName());
            }
        }

        if (merge.whenNotMatched() != null) {
            sql.append(" WHEN NOT MATCHED THEN ");
            sql.append(insertSqlGenerator.prepareSql(merge.whenNotMatched(), true, connectionProvider));
        }

        return sql.toString();
    }
}
