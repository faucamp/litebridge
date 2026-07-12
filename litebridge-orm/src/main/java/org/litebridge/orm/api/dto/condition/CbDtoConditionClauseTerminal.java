package org.litebridge.orm.api.dto.condition;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.AbstractConditionClauseStart;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;

/**
 * Implementation of a terminal condition clause for DTO-based queries.
 *
 * @param <DTO> The type of the DTO being queried.
 */
public final class CbDtoConditionClauseTerminal<DTO> extends AbstractCbConditionClauseTerminal<DTO> {

    private final OrmTable ormTable;

    /**
     * Constructs a new {@code CbDtoConditionClauseTerminal}.
     *
     * @param conditionGroupSpec The condition group specification.
     * @param ormTable           The ORM table metadata.
     * @param fromClauseEngine   The FROM clause engine.
     */
    public CbDtoConditionClauseTerminal(final ConditionGroupSpec conditionGroupSpec, final OrmTable ormTable, final FromClauseEngine fromClauseEngine) {
        super(conditionGroupSpec, fromClauseEngine);
        this.ormTable = ormTable;
    }


    @Override
    protected CbDtoConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final String field) {
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
        return (CbDtoConditionClause<DTO>) whereImpl(logicOperator, new SelectColumnSpec(column));
    }

    @Override
    protected AbstractCbConditionClause<DTO> createCbConditionClause(final ConditionSpec conditionSpec) {
        return new CbDtoConditionClause<>(conditionSpec, conditionGroupSpec, ormTable, fromClauseEngine);
    }

    @Override
    protected AbstractConditionClauseStart<DTO> createConditionClauseStart(final ConditionGroupSpec subgroup) {
        return new DtoConditionClauseStart<>(subgroup, ormTable, fromClauseEngine);
    }
}
