package org.litebridgedb.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.db.spi.expression.LiteralExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;
import org.litebridgedb.db.spi.expression.SelectReference;
import org.litebridgedb.db.spi.expression.SubselectExpression;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

import java.util.Collection;
import java.util.List;

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

    public ExpressionSpec getLhs() {
        return lhs;
    }

    public void setLhs(final ExpressionSpec lhs) {
        this.lhs = lhs;
    }

    public void setLhs(final Column column) {
        this.lhs = new SelectColumnSpec(column);
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

        final LiteralExpression literalExpression = selectExpressionMapper.sqlFunctionRegistry().select().literal().create(value);
        return new Condition(lhsSelectExpression, operator, literalExpression);
    }
}
