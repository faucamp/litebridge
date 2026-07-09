package org.litebridgedb.orm.api.dto.condition;

import org.litebridgedb.orm.api.condition.AbstractCbConditionClause;
import org.litebridgedb.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.persistence.OrmTable;

public class CbDtoConditionClause<DTO> extends AbstractCbConditionClause<DTO> {

    private final OrmTable ormTable;

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
