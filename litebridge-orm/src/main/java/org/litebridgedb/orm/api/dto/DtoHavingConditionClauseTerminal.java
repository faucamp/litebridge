package org.litebridgedb.orm.api.dto;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.orm.api.select.HavingConditionClauseTerminal;
import org.litebridgedb.orm.api.select.impl.AbstractHavingClauseTerminal;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;
import org.litebridgedb.orm.persistence.OrmTable;

import java.util.Arrays;

public final class DtoHavingConditionClauseTerminal<DTO>
        extends AbstractHavingClauseTerminal<DTO,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>,
        DtoSelectSpec>

        implements HavingConditionClauseTerminal<DTO,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    private final OrmTable table;

    public DtoHavingConditionClauseTerminal(final DtoSelector<DTO> delegate) {
        super(delegate);
        table = delegate.table();
    }

    /**
     * Adds an "AND" condition to the current condition clause using the specified lhs.
     * This method is used to chain additional conditions in a SQL query in a type-safe and fluent manner.
     *
     * @param field the name of the DTO field to be used in the "AND" condition
     * @return the parent condition clause interface, allowing further chaining of conditions
     */
    @Override
    public DtoHavingConditionClause<DTO> and(final String field) {
        Column column = table.getColumnForFieldName(field).toColumn();

        // Use the aliased lhs if it is part of the SELECT clause, else use the unaliased lhs
        for (final ExpressionSpec expressionSpec : selectSpec.getExpressions()) {
            Column selectedColumn;

            if (expressionSpec instanceof SelectFieldSpec selectFieldSpec) {
                selectedColumn = selectFieldSpec.column();
            } else {
                continue;
            }

            if (selectedColumn.equalsIgnoreAlias(column)) {
                column = selectedColumn;
                break;
            }
        }

        return new DtoHavingConditionClause<>(selectSpec.newWhereCondition(column), this, delegate.litebridgeContext());
    }

    /**
     * Adds an "AND" condition to the current condition clause using the specified lhs.
     * This method is used to chain additional conditions in a SQL query in a type-safe and fluent manner.
     *
     * @param field the field to be used in the "AND" condition
     * @return the parent condition clause interface, allowing further chaining of conditions
     */
    @Override
    public DtoHavingConditionClause<DTO> and(final FieldColumnSpec field) {
        return and(field.field().name());
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return orderByImpl(Arrays.stream(fields)
                .map(table::getColumnForFieldName)
                .map(ColumnMetaData::name)
                .toArray(String[]::new));
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final FieldColumnSpec... fields) {
        return orderByImpl(Arrays.stream(fields)
                .map(field -> field.columnSpec().name())
                .toArray(String[]::new));
    }

    private DtoOrderByClause<DTO> orderByImpl(final String[] columns) {
        return new DtoOrderByClause<>(selectSpec.newOrderBy(columns), (DtoSelector<DTO>) delegate);
    }
}
