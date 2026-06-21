package org.litebridgedb.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.expression.LiteralExpression;
import org.litebridgedb.db.spi.expression.SelectReference;
import org.litebridgedb.db.spi.expression.SubselectExpression;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.orm.expression.ExpressionSpec;

/**
 * Specification for a condition in a database query.
 * <p>
 * This is used in a SQL query WHERE clause, JOIN clause, etc.
 * <p>
 * A condition consists of a column, an operator, and an optional value. The operator
 * dictates how the column will be compared to the provided value.
 */
public class ConditionSpec {

    private Column column;
    private Operator operator;
    private @Nullable Object value;

    public Column getColumn() {
        return column;
    }

    public void setColumn(final Column column) {
        this.column = column;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(final Operator operator) {
        this.operator = operator;
    }

    public @Nullable Object getValue() {
        return value;
    }

    public void setValue(final @Nullable Object value) {
        this.value = value;
    }

    public Condition toCondition(final SelectExpressionMapper selectExpressionMapper) {
        if (value instanceof SelectSpec selectSpec) {
            final Select select = selectSpec.toSelect();
            final SubselectExpression subselectExpression = selectExpressionMapper.sqlFunctionRegistry().select().subselect().create(select);
            return new Condition(column, operator, subselectExpression);
        } else if (value instanceof ExpressionSpec expressionSpec) {
            return new Condition(column, operator, selectExpressionMapper.toSelectExpression(expressionSpec));
        } else if (value instanceof Column referencedColumn) {
            // Reference to a selected column
            final SelectReference selectReference = selectExpressionMapper.sqlFunctionRegistry().select().reference().create(referencedColumn);
            return new Condition(column, operator, selectReference);
        }

        final LiteralExpression literalExpression = selectExpressionMapper.sqlFunctionRegistry().select().literal().create(value);
        return new Condition(column, operator, literalExpression);
    }
}
