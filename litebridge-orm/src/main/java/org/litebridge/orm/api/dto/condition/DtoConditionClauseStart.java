package org.litebridge.orm.api.dto.condition;

import org.litebridge.db.spi.Column;
import org.litebridge.orm.api.condition.AbstractConditionClauseStart;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;

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
