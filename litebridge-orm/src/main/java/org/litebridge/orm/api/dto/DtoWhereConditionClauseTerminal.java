package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractWhereClauseTerminal;
import org.litebridge.orm.api.spec.FieldColumnSpec;
import org.litebridge.orm.persistence.OrmTable;

import java.util.Arrays;

public final class DtoWhereConditionClauseTerminal<DTO>
        extends AbstractWhereClauseTerminal<DTO,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>,
        DtoSelectSpec>

        implements WhereConditionClauseTerminal<DTO,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    private final OrmTable table;

    public DtoWhereConditionClauseTerminal(final DtoSelector<DTO> delegate) {
        super(delegate);
        table = delegate.table();
    }

    @Override
    public DtoWhereConditionClause<DTO> and(final String field) {
        Column column = table.getColumnForFieldName(field).toColumn();

        // Use the aliased column if it is part of the SELECT clause, else use the unaliased column
        for (Column selectedColumn : selectSpec.columns()) {
            if (selectedColumn.equalsIgnoreAlias(column)) {
                column = selectedColumn;
                break;
            }
        }

        return new DtoWhereConditionClause<>(selectSpec.newWhereCondition(column), this);
    }

    public DtoWhereConditionClause<DTO> and(final FieldColumnSpec field) {
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
        return new DtoOrderByClause<>(selectSpec.newOrderBy(columns), delegate);
    }
}
