package org.litebridgedb.orm.api.dto.condition;

import org.litebridgedb.orm.api.condition.AbstractCbConditionClause;
import org.litebridgedb.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.persistence.OrmTable;

/**
 * Implementation of a condition clause for DTO-based queries.
 *
 * @param <DTO> The type of the DTO being queried.
 */
public class CbDtoConditionClause<DTO> extends AbstractCbConditionClause<DTO> {

    private final OrmTable ormTable;

    /**
     * Constructs a new {@code CbDtoConditionClause}.
     *
     * @param conditionSpec      The condition specification.
     * @param conditionGroupSpec The condition group specification.
     * @param ormTable           The ORM table metadata.
     * @param fromClauseEngine   The FROM clause engine.
     */
    public CbDtoConditionClause(final ConditionSpec conditionSpec,
                                final ConditionGroupSpec conditionGroupSpec,
                                final OrmTable ormTable,
                                final FromClauseEngine fromClauseEngine) {
        super(conditionSpec, conditionGroupSpec, fromClauseEngine);
        this.ormTable = ormTable;
    }

    @Override
    protected AbstractCbConditionClauseTerminal<DTO> createCbConditionClauseTerminal() {
        return new CbDtoConditionClauseTerminal<>(conditionGroupSpec, ormTable, fromClauseEngine);
    }
}
