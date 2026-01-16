package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.orm.api.select.impl.AbstractFromClauseTerminal;
import org.litebridge.orm.api.spec.FieldColumnSpec;
import org.litebridge.orm.persistence.OrmTable;

import java.util.Arrays;

public final class DtoFromClauseTerminal<DTO> extends AbstractFromClauseTerminal<DTO,
        DtoJoinClause<DTO>,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>>

        implements DtoJoinClassTerminal<DTO> {

    private final OrmTable table;

    public DtoFromClauseTerminal(final DtoSelector<DTO> delegate) {
        super(delegate);
        table = delegate.table();
    }

    @Override
    public DtoWhereConditionClause<DTO> where(final String field) {
        final Column column = table.getColumnForFieldName(field);
        return new DtoWhereConditionClause<>(selectSpec.newWhereCondition(column), new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate));
    }

    public DtoWhereConditionClause<DTO> where(final FieldColumnSpec field) {
        return where(field.field().name());
    }

    @Override
    public DtoJoinClause<DTO> join(final Class<?> dtoClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        final String[] columns = Arrays.stream(fields)
                .map(table::getColumnForFieldName)
                .map(ColumnMetaData::name)
                .toArray(String[]::new);
        return new DtoOrderByClause<>(selectSpec.newOrderBy(columns), delegate);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final FieldColumnSpec... fields) {
        final String[] columns = Arrays.stream(fields)
                .map(fieldColumnSpec -> fieldColumnSpec.column().name())
                .toArray(String[]::new);
        return new DtoOrderByClause<>(selectSpec.newOrderBy(columns), delegate);
    }
}
