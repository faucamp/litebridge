package org.litebridgedb.maven.config;

import org.apache.maven.plugins.annotations.Parameter;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Table mapping customisation configuration for reverse engineering.
 */
public class TableMappingConfig {

    /**
     * Table name.
     * <p>
     * This table must be present in the set of input tables.
     */
    @Parameter(required = true)
    private String table;

    /**
     * Specify the generated entity class name explicitly.
     * <p>
     * If specified, this name will be used for the resulting class.
     * If not specified, the classname is generated from the table name.
     */
    private @Nullable String entityName;

    /**
     * Configuration for specific columns.
     */
    private @Nullable List<ColumnMappingConfig> columnMappings;

    public String getTable() {
        return table;
    }

    public void setTable(final String table) {
        this.table = table;
    }

    public @Nullable String getEntityName() {
        return entityName;
    }

    public void setEntityName(final @Nullable String entityName) {
        this.entityName = entityName;
    }

    public @Nullable List<ColumnMappingConfig> getColumnMappings() {
        return columnMappings;
    }

    public void setColumnMappings(final @Nullable List<ColumnMappingConfig> columnMappings) {
        this.columnMappings = columnMappings;
    }
}
