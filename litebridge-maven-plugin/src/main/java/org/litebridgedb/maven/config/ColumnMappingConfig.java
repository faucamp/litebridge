package org.litebridgedb.maven.config;

import org.apache.maven.plugins.annotations.Parameter;
import org.jspecify.annotations.Nullable;

/**
 * Column mapping customisation configuration for reverse engineering.
 */
public final class ColumnMappingConfig {

    /**
     * Column name.
     */
    @Parameter(required = true)
    private String column;

    /**
     * Specify an explicit name for the mapped field.
     */
    private @Nullable String fieldName;

    /**
     * Specify an explicit type for the mapped field.
     */
    private @Nullable String fieldType;

    /**
     * If this column should be generated using a sequence, specify the name of the sequence here.
     * <p>
     * This cannot be used in conjunction with {@code generatorClass}.
     */
    private @Nullable String generateUsingSequence;

    /**
     * Specify a custom generator class to use for generating values for this column.
     * <p>
     * This cannot be used in conjunction with {@code generateUsingSequence}.
     */
    private @Nullable String generatorClass;

    public String getColumn() {
        return column;
    }

    public void setColumn(final String column) {
        this.column = column;
    }

    public @Nullable String getFieldName() {
        return fieldName;
    }

    public void setFieldName(final @Nullable String fieldName) {
        this.fieldName = fieldName;
    }

    public @Nullable String getFieldType() {
        return fieldType;
    }

    public void setFieldType(final @Nullable String fieldType) {
        this.fieldType = fieldType;
    }

    public @Nullable String getGenerateUsingSequence() {
        return generateUsingSequence;
    }

    public void setGenerateUsingSequence(final @Nullable String generateUsingSequence) {
        this.generateUsingSequence = generateUsingSequence;
    }

    public @Nullable String getGeneratorClass() {
        return generatorClass;
    }

    public void setGeneratorClass(final @Nullable String generatorClass) {
        this.generatorClass = generatorClass;
    }
}
