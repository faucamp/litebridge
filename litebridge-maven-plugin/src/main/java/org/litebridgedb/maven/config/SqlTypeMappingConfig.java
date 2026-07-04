package org.litebridgedb.maven.config;

import org.apache.maven.plugins.annotations.Parameter;
import org.jspecify.annotations.Nullable;

import java.sql.JDBCType;

/**
 * SQL type mapping customisation configuration for reverse engineering.
 */
public class SqlTypeMappingConfig {

    /**
     * Name of the JDBC SQL type.
     * <p>
     * Example: {@code NUMERIC}
     */
    @Parameter(required = true)
    private JDBCType jdbcType;

    /**
     * Precision of the SQL type.
     * <p>
     * Example: {@code 1}
     */
    private @Nullable Integer precision;

    /**
     * Whether the SQL type is not null.
     */
    private @Nullable Boolean notNull;

    /**
     * Java class to use for this JDBC type.
     * <p>
     * Example: {@code java.lang.Long}
     */
    @Parameter(required = true)
    private String fieldType;

    public JDBCType getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(final JDBCType jdbcType) {
        this.jdbcType = jdbcType;
    }

    public @Nullable Integer getPrecision() {
        return precision;
    }

    public void setPrecision(final @Nullable Integer precision) {
        this.precision = precision;
    }

    public @Nullable Boolean getNotNull() {
        return notNull;
    }

    public void setNotNull(final @Nullable Boolean notNull) {
        this.notNull = notNull;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(final String fieldType) {
        this.fieldType = fieldType;
    }
}
