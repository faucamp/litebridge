package org.litebridgedb.maven.config;

import org.apache.maven.plugins.annotations.Parameter;

public class ColumnMappingConfig {

    /**
     * Column name.
     */
    @Parameter(required = true)
    private String column;

    /**
     * Specify an explicit name for the mapped field.
     */
    private String fieldName;

    /**
     * Specify an explicit type for the mapped field.
     */
    private String fieldType;

    /**
     * If this column should be generated using a sequence, specify the name of the sequence here.
     * <p>
     * This cannot be used in conjunction with {@code generatorClass}.
     */
    private String generateUsingSequence;

    /**
     * Specify a custom generator class to use for generating values for this column.
     * <p>
     * This cannot be used in conjunction with {@code generateUsingSequence}.
     */
    private String generatorClass;

    public String getColumn() {
        return column;
    }

    public void setColumn(final String column) {
        this.column = column;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(final String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(final String fieldType) {
        this.fieldType = fieldType;
    }

    public String getGenerateUsingSequence() {
        return generateUsingSequence;
    }

    public void setGenerateUsingSequence(final String generateUsingSequence) {
        this.generateUsingSequence = generateUsingSequence;
    }

    public String getGeneratorClass() {
        return generatorClass;
    }

    public void setGeneratorClass(final String generatorClass) {
        this.generatorClass = generatorClass;
    }
}
