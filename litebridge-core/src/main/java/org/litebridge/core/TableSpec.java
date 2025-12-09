package org.litebridge.core;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Map;

public final class TableSpec {

    @Nullable
    private final String catalog;
    @Nullable
    private final String schema;
    @Nonnull
    private final String table;
    @Nonnull
    private final Map<String, ColumnSpec> fieldColumnSpecMap;

    public TableSpec(@Nullable final String catalog, @Nullable final String schema, @Nonnull final String table, @Nonnull final Map<String, ColumnSpec> fieldColumnSpecMap) {
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
        this.fieldColumnSpecMap = fieldColumnSpecMap;
    }

    public @Nullable String getCatalog() {
        return catalog;
    }

    public @Nullable String getSchema() {
        return schema;
    }

    public @Nonnull String getTable() {
        return table;
    }

    public @Nonnull Map<String, ColumnSpec> getFieldColumnSpecMap() {
        return fieldColumnSpecMap;
    }

    public static TableSpec t(final String catalog, final String schema, final String table, final Map<String, ColumnSpec> fieldColumnSpecMap) {
        return new TableSpec(catalog, schema, table, fieldColumnSpecMap);
    }

    public static TableSpec t(final String schema, final String table, final Map<String, ColumnSpec> fieldColumnSpecMap) {
        return new TableSpec(null, schema, table, fieldColumnSpecMap);
    }

    public static TableSpec t(final String table, final Map<String, ColumnSpec> fieldColumnSpecMap) {
        return new TableSpec(null, null, table, fieldColumnSpecMap);
    }
}
