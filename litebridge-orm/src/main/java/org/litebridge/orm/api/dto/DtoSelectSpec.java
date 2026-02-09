package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.tracking.FieldAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class DtoSelectSpec extends SelectSpec implements DtoDataSpec {

    private final Class<?> dtoClass;
    private final OrmTable dtoTable;
    @Nullable
    private String dtoAlias;
    @Nullable
    private List<FieldColumn> fieldColumns;

    public DtoSelectSpec(final Class<?> dtoClass, final OrmTable dtoTable) {
        this.dtoClass = dtoClass;
        this.dtoTable = dtoTable;
    }

    public Class<?> dtoClass() {
        return dtoClass;
    }

    @Override
    public OrmTable dtoTable() {
        return dtoTable;
    }

    public @Nullable String getDtoAlias() {
        return dtoAlias;
    }

    public void setDtoAlias(final @Nullable String dtoAlias) {
        this.dtoAlias = dtoAlias;
    }

    public List<FieldColumn> getFieldColumns() {
        return fieldColumns;
    }

    public void setFieldColumns(final List<FieldColumn> fieldColumns) {
        this.fieldColumns = new ArrayList<>(fieldColumns);
        super.setColumns(fieldColumns.stream().map(FieldColumn::column).toList());
    }

    public record FieldColumn(FieldAccessor fieldAccessor, Column column) {
    }

    public void addFieldColumns(final List<FieldColumn> fieldColumns) {
        final List<FieldColumn> rootDtoFieldColumns = fieldColumns.stream()
                .filter(fieldColumn -> fieldColumn.fieldAccessor().dtoClass() == dtoClass)
                .toList();

        if (!rootDtoFieldColumns.isEmpty()) {
            if (this.fieldColumns == null) {
                this.fieldColumns = new ArrayList<>(fieldColumns.stream()
                        .filter(fieldColumn -> fieldColumn.fieldAccessor().dtoClass() == dtoClass)
                        .toList());
            } else {
                this.fieldColumns.addAll(fieldColumns.stream()
                        .filter(fieldColumn -> fieldColumn.fieldAccessor().dtoClass() == dtoClass)
                        .toList());
            }
        }

        super.addColumns(fieldColumns.stream()
                .map(FieldColumn::column)
                .toList());
    }

    @Override
    public void addColumns(final Collection<? extends Column> columns) {

    }

    @Override
    public void setColumns(final List<Column> columns) {
        throw methodNotSupported("setFieldColumns(List<FieldColumn> fieldColumns)");
    }

    public DtoJoinSpec newJoinSpec(final Class<?> dtoClass, final OrmTable table) {
        if (this.joins == null) {
            joins = new ArrayList<>();
        }

        final DtoJoinSpec joinSpec = new DtoJoinSpec(dtoClass, table);
        joins.add(joinSpec);
        return joinSpec;
    }

    @Override
    public JoinSpec newJoinSpec(final String schema, final String table) {
        throw methodNotSupported("newJoinSpec(Class<?> dtoClass, Table table)");
    }

    @Override
    public JoinSpec newJoinSpec(final String table) {
        throw methodNotSupported("newJoinSpec(Class<?> dtoClass, Table table)");
    }

    @Override
    public JoinSpec newJoinSpec(final Table table) {
        throw methodNotSupported("newJoinSpec(Class<?> dtoClass, Table table)");
    }

    private static UnsupportedOperationException methodNotSupported(final String alternative) {
        return new UnsupportedOperationException("Not supported; use '%s' instead".formatted(alternative));
    }
}
