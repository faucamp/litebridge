package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.Join;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.persistence.OrmTable;

import java.util.ArrayList;
import java.util.List;

public final class DtoJoinSpec implements JoinSpec, DtoDataSpec {

    private final Class<?> dtoClass;
    private final OrmTable ormTable;
    private final Table table;
    private final List<ConditionSpec> conditions = new ArrayList<>();
    @Nullable
    private List<DtoSelectSpec.FieldColumn> fieldColumns;

    public DtoJoinSpec(final Class<?> dtoClass, final OrmTable ormTable, final Table table) {
        this.dtoClass = dtoClass;
        this.ormTable = ormTable;
        this.table = table;
    }

    public Class<?> dtoClass() {
        return dtoClass;
    }

    @Override
    public OrmTable dtoTable() {
        return ormTable;
    }

    public @Nullable List<DtoSelectSpec.FieldColumn> getFieldColumns() {
        return fieldColumns;
    }

    public void setFieldColumns(@Nullable final List<DtoSelectSpec.FieldColumn> fieldColumns) {
        this.fieldColumns = fieldColumns;
    }

    @Override
    public Table table() {
        return table;
    }

    @Override
    public List<ConditionSpec> conditions() {
        return conditions;
    }

    public ConditionSpec newCondition(final Column column) {
        ObjectUtils.requireNonNull(column.alias(), () -> new IllegalArgumentException("Column alias not specified"));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditions.add(conditionSpec);
        return conditionSpec;
    }

    @Override
    public Join toJoin() {
        return new Join(table, conditions.stream()
                .map(conditionSpec -> new Condition(conditionSpec.getColumn(),
                        conditionSpec.getOperator(),
                        conditionSpec.getValue()))
                .toList());
    }
}
