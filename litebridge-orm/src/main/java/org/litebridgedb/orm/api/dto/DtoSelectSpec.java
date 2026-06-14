package org.litebridgedb.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.function.SqlFunctionRegistry;
import org.litebridgedb.db.spi.query.SelectExpression;
import org.litebridgedb.orm.api.select.model.SelectSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.tracking.FieldAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DtoSelectSpec extends SelectSpec implements DtoDataSpec {

    private final Class<?> dtoClass;
    private final OrmTable dtoTable;
    @Nullable
    private List<FieldColumn> fieldColumns;

    public DtoSelectSpec(final Class<?> dtoClass, final OrmTable dtoTable, final AliasGenerator aliasGenerator, final SqlFunctionRegistry sqlFunctionRegistry) {
        super(sqlFunctionRegistry);
        this.dtoClass = dtoClass;
        this.dtoTable = dtoTable;
        this.table = aliasGenerator.aliasTable(dtoTable);
    }

    public Class<?> dtoClass() {
        return dtoClass;
    }

    @Override
    public OrmTable dtoTable() {
        return dtoTable;
    }

    public List<FieldColumn> getFieldColumns() {
        return ObjectUtils.requireNonNull(fieldColumns, () -> new IllegalStateException("DtoSelectSpec.fieldColumns not set"));
    }

    public void setFieldColumns(final List<FieldColumn> fieldColumns) {
        this.fieldColumns = new ArrayList<>(fieldColumns);
    }

    @Override
    protected List<SelectExpression> expressions() {
        return fieldColumns != null ?
                fieldColumns.stream()
                        .map(fieldColumn -> (SelectExpression) sqlFunctionRegistry.selectColumnFactory().create(fieldColumn.column()))
                        .toList()
                : Collections.emptyList();
    }

    public record FieldColumn(FieldAccessor fieldAccessor, Column column) {
    }

    public void addFieldColumns(final List<FieldColumn> fieldColumns) {
        if (this.fieldColumns == null) {
            this.fieldColumns = new ArrayList<>(fieldColumns);
        } else {
            this.fieldColumns.addAll(fieldColumns);
        }
    }

    public DtoJoinSpec newJoinSpec(final Class<?> dtoClass, final OrmTable ormTable, final Table table) {
        return addNewJoinSpecBefore(null, dtoClass, ormTable, table);
    }

    public DtoJoinSpec newJoinSpecBefore(final DtoJoinSpec other, final Class<?> dtoClass, final OrmTable ormTable, final Table table) {
        return addNewJoinSpecBefore(other, dtoClass, ormTable, table);
    }

    private DtoJoinSpec addNewJoinSpecBefore(final @Nullable DtoJoinSpec other, final Class<?> dtoClass, final OrmTable ormTable, final Table table) {
        if (this.joins == null) {
            joins = new ArrayList<>();
        }

        final DtoJoinSpec joinSpec = new DtoJoinSpec(dtoClass, ormTable, table);

        if (other != null) {
            joins.add(joins.indexOf(other), joinSpec);
        } else {
            joins.add(joinSpec);
        }

        return joinSpec;
    }
}
