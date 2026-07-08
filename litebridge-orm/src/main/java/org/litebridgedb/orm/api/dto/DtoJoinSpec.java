package org.litebridgedb.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Join;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.JoinSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.persistence.OrmTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DtoJoinSpec implements JoinSpec, DtoDataSpec {

    private final Class<?> dtoClass;
    private final OrmTable ormTable;
    private final Table table;
    private final List<ConditionGroupSpec> conditions = new ArrayList<>();
    private final SelectExpressionMapper selectExpressionMapper;
    @Nullable
    private List<DtoSelectSpec.FieldColumn> fieldColumns;

    public DtoJoinSpec(final Class<?> dtoClass, final OrmTable ormTable, final Table table, final SelectExpressionMapper selectExpressionMapper) {
        this.dtoClass = dtoClass;
        this.ormTable = ormTable;
        this.table = table;
        this.selectExpressionMapper = selectExpressionMapper;
    }

    public Class<?> dtoClass() {
        return dtoClass;
    }

    @Override
    public OrmTable dtoTable() {
        return ormTable;
    }

    public List<DtoSelectSpec.FieldColumn> getFieldColumns() {
        return fieldColumns != null ? fieldColumns : Collections.emptyList();
    }

    public void setFieldColumns(@Nullable final List<DtoSelectSpec.FieldColumn> fieldColumns) {
        this.fieldColumns = fieldColumns;
    }

    @Override
    public Table table() {
        return table;
    }

    @Override
    public List<ConditionGroupSpec> conditions() {
        return conditions;
    }

    public ConditionGroupSpec newConditionGroup(final LogicOperator logicOperator) {
        final ConditionGroupSpec conditionGroupSpec = new ConditionGroupSpec(logicOperator);
        conditions.add(conditionGroupSpec);
        return conditionGroupSpec;
    }

    @Override
    public Join toJoin() {
        return new Join(table, conditions.stream()
                .map(conditionGroupSpec -> conditionGroupSpec.toConditionGroup(selectExpressionMapper, Collections.singleton(table)))
                .toList());
    }
}
