package org.litebridge.db.oracle.sql;

import org.litebridge.commons.BooleanUtils;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.sql.InsertSqlGenerator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.UpdateColumn;

import java.util.function.BiFunction;

public final class OracleInsertSqlGenerator extends InsertSqlGenerator {

    /**
     * Creates a new {@code OracleInsertSqlGenerator}.
     *
     * @param columnIdentifierGenerator the column identifier generator
     * @param ensureTableMetaData       a function to ensure table metadata
     */
    public OracleInsertSqlGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator,
                                    final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(columnIdentifierGenerator, ensureTableMetaData);
    }

    @Override
    public String prepareSql(final Insert insert, final ConnectionProvider connectionProvider) {
        if (insert.rows() == 1) {
            return super.prepareSql(insert, connectionProvider);
        }

        return createInsertAllClause(insert);
    }

    private String createInsertAllClause(final Insert insert) {
        BooleanUtils.requireFalse(insert.returnGeneratedKeys(), "INSERT ALL cannot return generated keys");

        final StringBuilder sql = new StringBuilder("INSERT ALL ");
        final String intoClause = createInsertIntoClause(insert);

        for (int i = 0; i < insert.rows(); i++) {
            sql.append(intoClause).append('(');

            for (int j = 0; j < insert.columns().size(); j++) {
                final UpdateColumn insertColumn = insert.columns().get(j);

                if (j > 0) {
                    sql.append(", ");
                }

                if (insertColumn.generatedValue() != null) {
                    sql.append(insertColumn.generatedValue());
                } else {
                    sql.append('?');
                }
            }

            sql.append(") ");
        }

        sql.append("SELECT * FROM DUAL");
        return sql.toString();
    }

    private String createInsertIntoClause(final Insert insert) {
        final StringBuilder intoClause = new StringBuilder("INTO ");
        appendTable(intoClause, insert.table())
                .append(" (")
                .append(String.join(", ", insert.columns().stream()
                        .map(UpdateColumn::name)
                        .map(columnIdentifierGenerator::quoteIdentifier)
                        .toList()))
                .append(") VALUES ");
        return intoClause.toString();
    }
}
