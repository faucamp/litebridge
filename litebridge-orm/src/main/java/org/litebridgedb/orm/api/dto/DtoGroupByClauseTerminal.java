package org.litebridgedb.orm.api.dto;

import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.select.impl.AbstractGroupByClauseTerminal;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.persistence.OrmTable;

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
        final ConditionGroupSpec conditionGroupSpec = selectSpec.newHavingConditionGroup(LogicOperator.AND);
        return new DtoHavingConditionClause<>(conditionGroupSpec.newCondition(expression),
                new DtoHavingConditionClauseTerminal<>((DtoSelector<DTO>) delegate),
                delegate.litebridgeContext());
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return new DtoOrderByClause<>(selectSpec.newOrderBy(selectSpec.createSelectFieldSpecs(fields)), (DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(selectSpec.newOrderBy(fields), (DtoSelector<DTO>) delegate);
    }
}
