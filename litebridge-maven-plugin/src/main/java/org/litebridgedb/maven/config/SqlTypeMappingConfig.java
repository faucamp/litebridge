package org.litebridgedb.maven.config;

import org.apache.maven.plugins.annotations.Parameter;

import java.sql.JDBCType;

public class SqlTypeMappingConfig {

    /**
     * Name of the SQL type.
     * <p>
     * Example: {@code NUMERIC}
     */
    @Parameter(required = true)
    private JDBCType sqlType;

    /**
     * Precision of the SQL type.
     * <p>
     * Example: {@code 1}
     */
    private Integer precision;

    /**
     * Whether the SQL type is not null.
     */
    private Boolean notNull;

    /**
     * Java class to use for this SQL type.
     * <p>
     * Example: {@code java.lang.Long}
     */
    @Parameter(required = true)
    private String fieldType;

    public JDBCType getSqlType() {
        return sqlType;
    }

    public void setSqlType(final JDBCType sqlType) {
        this.sqlType = sqlType;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(final Integer precision) {
        this.precision = precision;
    }

    public Boolean getNotNull() {
        return notNull;
    }

    public void setNotNull(final Boolean notNull) {
        this.notNull = notNull;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(final String fieldType) {
        this.fieldType = fieldType;
    }
}
