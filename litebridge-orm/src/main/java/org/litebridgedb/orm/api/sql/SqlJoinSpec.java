package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.api.select.impl.AbstractJoinSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

/**
 * Specification for a JOIN operation in a database query.
 * <p>
 * This class includes the table to be joined and the conditions that establish the relationship between the tables.
 */
public class SqlJoinSpec extends AbstractJoinSpec {

    public SqlJoinSpec(final Table table, final SelectExpressionMapper selectExpressionMapper) {
        super(table, selectExpressionMapper);
    }

    public ConditionSpec using(final String column) {
        final Column spiColumn = new Column(table, column);
        final ConditionSpec usingCondition = conditions.newCondition(LogicOperator.NOOP, new SelectColumnSpec(spiColumn));
        usingCondition.setOperator(Operator.USING);
        return usingCondition;
    }
}
