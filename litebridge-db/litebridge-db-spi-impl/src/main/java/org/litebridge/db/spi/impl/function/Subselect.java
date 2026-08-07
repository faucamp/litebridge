package org.litebridge.db.spi.impl.function;

import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.SubselectExpression;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;

/**
 * A subselect expression.
 */
public class Subselect extends SubselectExpression {

    /**
     * The select SQL generator.
     */
    private final SelectSqlGenerator selectSqlGenerator;

    /**
     * Creates a new {@code Subselect}.
     *
     * @param subselect          the subselect operation
     * @param selectSqlGenerator the select SQL generator
     */
    public Subselect(final Select subselect, final SelectSqlGenerator selectSqlGenerator) {
        super(subselect);
        this.selectSqlGenerator = selectSqlGenerator;
    }

    @Override
    public String toSql(final Operation operation, final ConnectionProvider connectionProvider) {
        return selectSqlGenerator.prepareSql(subselect, connectionProvider);
    }
}
