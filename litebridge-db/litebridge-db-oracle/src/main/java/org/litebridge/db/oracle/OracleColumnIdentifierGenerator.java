package org.litebridge.db.oracle;

import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;

public final class OracleColumnIdentifierGenerator extends ColumnIdentifierGenerator {

    @Override
    public String createSelectColumn(final Column column, final Operation operation, final ClauseType clause, final boolean nested) {
        if (!(operation instanceof final Select select)) {
            return super.createSelectColumn(column, operation, clause, nested);
        }

        if (shouldApplyTableQualifier(column, select)) {
            return super.createSelectColumn(column, operation, clause, nested);
        }

        final StringBuilder columnSql = new StringBuilder(quoteIdentifier(column.name()));

        if (clause == ClauseType.SELECT && !StringUtils.isBlank(column.alias())) {
            //noinspection DataFlowIssue
            columnSql.append(' ').append(createAliasDeclaration(quoteIdentifier(column.alias())));
        }

        return columnSql.toString();
    }

    @Override
    public String createColumnRef(final Column column, final Operation operation, final ClauseType clause) {
        if (!(operation instanceof final Select select)) {
            return super.createColumnRef(column, operation, clause);
        }

        if (shouldApplyTableQualifier(column, select)) {
            return super.createColumnRef(column, operation, clause);
        }

        return quoteIdentifier(column.name());
    }

    @Override
    public String createAliasDeclaration(final String alias) {
        return quoteIdentifier(alias);
    }

    private static boolean shouldApplyTableQualifier(final Column column, final Select select) {
        boolean applyTableQualifier = true;

        // If a JOIN USING is used in the select from/where/using clause, Oracle doesn't allow table qualifiers for the column
        for (Join join : select.joins()) {

            if (join.conditions().conditions().size() != 1
                    && join.conditions().subgroups().isEmpty()) {
                continue;
            }

            for (LogicCondition logicCondition : join.conditions().conditions()) {
                final Condition condition = logicCondition.condition();

                if (condition.operator() == Operator.USING
                        // JOIN USING <expression>
                        && condition.lhs() instanceof ColumnExpression columnExpression
                        // Same expression
                        && (columnExpression.column().name().equals(column.name())
                        // Same expression but from other side of join
                        || (columnExpression.column().equalsColumnOnlyIgnoreAlias(column)
                        && (select.table().equalsIgnoreAlias(column.table()) || join.table().equalsIgnoreAlias(column.table()))))) {
                    // Don't include table qualifiers
                    applyTableQualifier = false;
                    break;
                }
            }

            if (!applyTableQualifier) {
                break;
            }
        }

        return applyTableQualifier;
    }
}
