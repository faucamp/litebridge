package org.litebridgedb.orm.api.dto.condition;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.api.condition.AbstractConditionClauseStart;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.persistence.OrmTable;

public class DtoConditionClauseStart<DTO> extends AbstractConditionClauseStart<DTO> {

    private final OrmTable ormTable;

    public DtoConditionClauseStart(final ConditionGroupSpec conditionGroupSpec,
                                   final OrmTable ormTable,
                                   final FromClauseEngine fromClauseEngine) {
        super(conditionGroupSpec, fromClauseEngine);
        this.ormTable = ormTable;
    }

    @Override
    public CbDtoConditionClause<DTO> where(final String field) {
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
        return (CbDtoConditionClause<DTO>) where(new SelectColumnSpec(column));
    }

    @Override
    protected CbDtoConditionClause<DTO> createCbConditionClause(final ConditionSpec conditionSpec) {
        return new CbDtoConditionClause<>(conditionSpec, conditionGroupSpec, ormTable, fromClauseEngine);
    }
}
