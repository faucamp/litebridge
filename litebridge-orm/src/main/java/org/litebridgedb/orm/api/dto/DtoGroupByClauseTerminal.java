package org.litebridgedb.orm.api.dto;

import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.orm.api.select.impl.AbstractGroupByClauseTerminal;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.persistence.OrmTable;

import java.util.Arrays;

public class DtoGroupByClauseTerminal<DTO> extends AbstractGroupByClauseTerminal<DTO,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>,
        DtoSelectSpec> {

    private final OrmTable ormTable;

    public DtoGroupByClauseTerminal(final DtoSelector<DTO> delegate) {
        super(delegate);
        this.ormTable = delegate.table();
    }

    @Override
    public DtoHavingConditionClause<DTO> having(final ExpressionSpec expression) {
        return null;
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return orderByImpl(Arrays.stream(fields)
                .map(ormTable::getColumnForFieldName)
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
