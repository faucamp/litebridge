package org.litebridge.core;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.StringUtils;

import java.util.Map;

/**
 * Specification of a database tab, used to map DTO instances to to target tables.
 * <p>
 * This class is immutable and provides various factory methods to create instances
 * with different configurations.
 */
public final class TableSpec {

    /**
     * Database catalog name
     */
    @Nullable
    private final String catalog;
    /**
     * Database schema name
     */
    @Nullable
    private final String schema;
    /**
     * Database table name
     */
    @Nonnull
    private final String table;
    /**
     * Field name to ColumnSpec map; key is field name, value is the column definition
     */
    @Nonnull
    private final Map<String, ColumnSpec> fieldColumnSpecMap;

    public TableSpec(@Nullable final String catalog, @Nullable final String schema, @Nonnull final String table, @Nonnull final Map<String, ColumnSpec> fieldColumnSpecMap) {
        this.catalog = catalog;
        this.schema = schema;
        this.table = StringUtils.requireNonBlank(table, "Table name cannot be null");
        this.fieldColumnSpecMap = CollectionUtils.requireNonEmpty(fieldColumnSpecMap, "Field-column map cannot be null");
    }

    /**
     * @return Database catalog name
     */
    public @Nullable String getCatalog() {
        return catalog;
    }

    /**
     * @return Database schema name
     */
    public @Nullable String getSchema() {
        return schema;
    }

    /**
     * @return Database table name
     */
    public @Nonnull String getTable() {
        return table;
    }

    /**
     * Field name to {@link ColumnSpec} map; key is field name, value is the column definition
     *
     * @return field name-database column mapping
     */
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
