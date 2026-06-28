package org.litebridgedb.orm.api.dto;

import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.orm.api.select.impl.AbstractGroupByClauseTerminal;
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
        return new DtoHavingConditionClause<>(selectSpec.newHavingCondition(expression),
                new DtoHavingConditionClauseTerminal<>((DtoSelector<DTO>) delegate),
                delegate.litebridgeContext());
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return orderByImpl(Arrays.stream(fields)
                .map(ormTable::getColumnForFieldName)
                .map(ColumnMetaData::name)
                .toArray(String[]::new));
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return orderByImpl(selectSpec.mapExpressionsToColumns(fields));
    }

    private DtoOrderByClause<DTO> orderByImpl(final String[] columns) {
        return new DtoOrderByClause<>(selectSpec.newOrderBy(columns), (DtoSelector<DTO>) delegate);
    }
}
