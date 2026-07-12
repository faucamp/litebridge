package org.litebridgedb.orm.api.dto.condition;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.condition.AbstractCbConditionClause;
import org.litebridgedb.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridgedb.orm.api.condition.AbstractConditionClauseStart;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.persistence.OrmTable;

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
