package org.litebridge.maven.config.reverse;

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
     * Specifies additional superinterfaces of the entity class that will be recognised by Litebridge relational mapping
     * if used in collections.
     * <p>
     * For example, if the entity class is defined as {@code class MyEntity implements MyInterface},
     * then {@code MyInterface.class} should be specified via this setting to ensure that Litebridge can correctly handle
     * collections of {@code MyInterface} instances in related entities.
     * <p>
     * This is mostly useful if combining generated entities with hand-crafted entities or manually registered DTOs
     * that make use of this interface in collections.
     */
    private @Nullable String allowInterface;

    /**
     * Controls how related entities are mapped when foreign keys are encountered for this table.
     * <p>
     * Setting it to {@code false} disables entity resolution; foreign key fields will use the "flat" database type
     * (e.g. {@code Long personId}) instead of replacing the field with the related entity (e.g. {@code Person person}.
     * <p>
     * This setting overrides the global output setting with the same name, for this table.
     * <p>
     * Default: uses global setting
     */
    private @Nullable Boolean resolveRelationships;

    /**
     * Controls whether default values are initialised for fields representing columns with a default database value.
     * <p>
     * This setting overrides the global output setting with the same name, for this table.
     * <p>
     * Default: uses global setting
     */
    private @Nullable Boolean initDefaultValues;

    /**
     * Controls whether primitive fields are generated if the database column is not nullable, if applicable.
     * <p>
     * For example, if the non-null SQL type maps to a `java.lang.Long`, then a primitive `long` will be generated
     * instead of a `java.lang.Long`.
     * <p>
     * This setting overrides the global output setting with the same name, for this table.
     * <p>
     * Default: uses global setting
     */
    private @Nullable Boolean primitiveNotNulls;

    /**
     * Controls whether a default (no-arg) and a canonical (all-fields) constructor are generated for this entity.
     * <p>
     * This setting overrides the global output setting with the same name, for this table.
     * <p>
     * Default: uses global setting
     */
    private @Nullable Boolean generateConstructors;

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

    public @Nullable String getAllowInterface() {
        return allowInterface;
    }

    public void setAllowInterface(@Nullable final String allowInterface) {
        this.allowInterface = allowInterface;
    }

    public @Nullable Boolean getResolveRelationships() {
        return resolveRelationships;
    }

    public void setResolveRelationships(final Boolean resolveRelationships) {
        this.resolveRelationships = resolveRelationships;
    }

    public @Nullable Boolean getInitDefaultValues() {
        return initDefaultValues;
    }

    public void setInitDefaultValues(@Nullable final Boolean initDefaultValues) {
        this.initDefaultValues = initDefaultValues;
    }

    public @Nullable Boolean getPrimitiveNotNulls() {
        return primitiveNotNulls;
    }

    public void setPrimitiveNotNulls(@Nullable final Boolean primitiveNotNulls) {
        this.primitiveNotNulls = primitiveNotNulls;
    }

    public @Nullable Boolean getGenerateConstructors() {
        return generateConstructors;
    }

    public void setGenerateConstructors(@Nullable final Boolean generateConstructors) {
        this.generateConstructors = generateConstructors;
    }

    public @Nullable List<ColumnMappingConfig> getColumnMappings() {
        return columnMappings;
    }

    public void setColumnMappings(final @Nullable List<ColumnMappingConfig> columnMappings) {
        this.columnMappings = columnMappings;
    }
}
