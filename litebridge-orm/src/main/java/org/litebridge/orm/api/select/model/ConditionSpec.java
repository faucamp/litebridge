package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SelectReference;
import org.litebridge.db.spi.expression.SubselectExpression;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

/**
 * Specification for a condition in a database query.
 * <p>
 * This is used in a SQL query WHERE clause, JOIN clause, etc.
 * <p>
 * A condition consists of a column, an operator, and an optional value. The operator
 * dictates how the column will be compared to the provided value.
 */
public class ConditionSpec {

    private ExpressionSpec lhs;
    private Operator operator;
    private @Nullable Object value;

    /**
     * Gets the left-hand side expression of the condition.
     *
     * @return the LHS expression
     */
    public ExpressionSpec getLhs() {
        return lhs;
    }

    /**
     * Sets the left-hand side expression of the condition.
     *
     * @param lhs the LHS expression to set
     */
    public void setLhs(final ExpressionSpec lhs) {
        this.lhs = lhs;
    }

    /**
     * Sets the left-hand side expression of the condition to a specific column.
     *
     * @param column the column to set as LHS
     */
    public void setLhs(final Column column) {
        this.lhs = new SelectColumnSpec(column);
    }

    /**
     * Gets the operator used in the condition.
     *
     * @return the operator
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Sets the operator for the condition.
     *
     * @param operator the operator to set
     */
    public void setOperator(final Operator operator) {
        this.operator = operator;
    }

    /**
     * Gets the right-hand side value of the condition.
     *
     * @return the RHS value, or {@code null}
     */
    public @Nullable Object getValue() {
        return value;
    }

    /**
     * Sets the right-hand side value for the condition.
     *
     * @param value the RHS value to set
     */
    public void setValue(final @Nullable Object value) {
        this.value = value;
    }

    /**
     * Converts this specification into a {@link Condition}.
     *
     * @param selectExpressionMapper the mapper to use for expressions
     * @param selectedTables         the collection of tables included in the query
     * @return the resulting {@link Condition}
     */
    public Condition toCondition(final SelectExpressionMapper selectExpressionMapper, final Collection<Table> selectedTables) {
        final List<ExpressionSpec> lhsResolvedExpressionSpecs = selectExpressionMapper.resolveProtoExpression(lhs, ClauseType.WHERE).stream()
                .peek(expressionSpec -> {
                    if (expressionSpec instanceof ColumnExpressionSpec columnExpressionSpec) {
                        final Table expressionTable = columnExpressionSpec.getColumn().table();

                        // Override condtion column/table references to inherit the aliases from selected/joined tables if necessary
                        if (expressionTable.alias() == null) {
                            for (Table selectedTable : selectedTables) {
                                if (selectedTable.equalsIgnoreAlias(expressionTable)
                                        && !selectedTable.equals(expressionTable)) {
                                    columnExpressionSpec.setColumn(new Column(selectedTable, columnExpressionSpec.getColumn().name(), columnExpressionSpec.getColumn().alias()));
                                }
                            }
                        }
                    }
                })
                .toList();

        if (lhsResolvedExpressionSpecs.size() != 1) {
            throw new IllegalArgumentException("Expected exactly one LHS expression spec, but got " + lhsResolvedExpressionSpecs.size());
        }

        final SelectExpression lhsSelectExpression = selectExpressionMapper.toSelectExpression(lhsResolvedExpressionSpecs.getFirst(), true);

        if (value instanceof SelectSpec selectSpec) {
            final Select select = selectSpec.toSelect();
            final SubselectExpression subselectExpression = selectExpressionMapper.sqlFunctionRegistry().select().subselect().create(select);
            return new Condition(lhsSelectExpression, operator, subselectExpression);
        } else if (value instanceof ExpressionSpec expressionSpec) {
            final List<ExpressionSpec> rhsResolvedExpressionSpecs = selectExpressionMapper.resolveProtoExpression(expressionSpec, ClauseType.WHERE);

            if (rhsResolvedExpressionSpecs.size() != 1) {
                throw new IllegalArgumentException("Expected exactly one RHS expression spec, but got " + rhsResolvedExpressionSpecs.size());
            }

            return new Condition(lhsSelectExpression, operator, selectExpressionMapper.toSelectExpression(rhsResolvedExpressionSpecs.getFirst(), true));
        } else if (value instanceof Column referencedColumn) {
            // Reference to a selected column
            final SelectReference selectReference = selectExpressionMapper.sqlFunctionRegistry().select().reference().create(referencedColumn);
            return new Condition(lhsSelectExpression, operator, selectReference);
        }

        final LiteralExpression literalExpression = selectExpressionMapper.sqlFunctionRegistry().select().literal().create(value, true);
        return new Condition(lhsSelectExpression, operator, literalExpression);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ConditionSpec.class.getSimpleName() + "[", "]")
                .add("lhs=" + lhs)
                .add("operator=" + operator)
                .add("value=" + value)
                .toString();
    }
}
