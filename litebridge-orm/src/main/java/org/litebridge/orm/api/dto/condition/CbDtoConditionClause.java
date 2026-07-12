package org.litebridge.orm.api.dto.condition;

import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.persistence.OrmTable;

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
