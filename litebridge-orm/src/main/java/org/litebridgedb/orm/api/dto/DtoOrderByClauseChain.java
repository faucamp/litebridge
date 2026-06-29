package org.litebridgedb.orm.api.dto;

import org.litebridgedb.orm.api.select.OrderByClauseChain;
import org.litebridgedb.orm.api.select.impl.OrderByClauseTerminalImpl;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.persistence.OrmTable;

public final class DtoOrderByClauseChain<DTO>
        extends OrderByClauseTerminalImpl<DTO, DtoSelectSpec>
        implements OrderByClauseChain<DTO, DtoOrderByClause<DTO>, DtoOrderByClauseChain<DTO>> {

    private final OrmTable table;

    public DtoOrderByClauseChain(final DtoSelector<DTO> delegate) {
        super(delegate);
        table = delegate.table();
    }

    @Override
    public DtoOrderByClause<DTO> then(final String... fields) {
        return new DtoOrderByClause<>(selectSpec.newOrderBy(selectSpec.createSelectFieldSpecs(fields)), (DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoOrderByClause<DTO> then(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(selectSpec.newOrderBy(fields), (DtoSelector<DTO>) delegate);
    }
}
