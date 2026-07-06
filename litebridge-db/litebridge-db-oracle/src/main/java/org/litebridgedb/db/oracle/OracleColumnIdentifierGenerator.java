package org.litebridgedb.db.oracle;

import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.query.Join;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.db.spi.query.Select;

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
            for (Condition condition : join.conditions()) {
                if (condition.operator() == Operator.USING
                        // JOIN USING <expression>
                        && condition.lhs() instanceof ColumnExpression columnExpression
                        // Same expression
                        && (columnExpression.column().equalsIgnoreAlias(column)
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
