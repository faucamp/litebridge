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

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(final String fieldType) {
        this.fieldType = fieldType;
    }
}
