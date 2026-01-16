package org.litebridge.orm.api.spec;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Table;

import java.util.Collections;
import java.util.Map;

/**
 * Specification of a database tab, used to map DTO instances to to target tables.
 * <p>
 * This class is immutable and provides various factory methods to create instances
 * with different configurations.
 */
@NullMarked
public final class TableSpec extends Table {

    /**
     * Field name to ColumnSpec map; key is field name, value is the column definition
     */
    private final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap;

    public TableSpec(@Nullable final String catalog,
                     @Nullable final String schema,
                     final String table,
                     final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap) {
        super(StringUtils.blankIfNull(catalog),
                StringUtils.blankIfNull(schema),
                StringUtils.requireNonBlank(table, "Table name cannot be blank"));
        this.fieldColumnSpecMap = Collections.unmodifiableMap(CollectionUtils.requireNonEmpty(fieldColumnSpecMap, "Field-column map cannot be null"));
    }

    /**
     * Field name to {@link ColumnSpec} map; key is field name, value is the column definition
     *
     * @return field name-database column mapping
     */
    public Map<FieldSpec, ColumnSpec> fieldColumnSpecMap() {
        return fieldColumnSpecMap;
    }

    public static TableSpec t(final String catalog, final String schema, final String table, final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap) {
        return new TableSpec(catalog, schema, table, fieldColumnSpecMap);
    }

    public static TableSpec t(final String schema, final String table, final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap) {
        return new TableSpec(null, schema, table, fieldColumnSpecMap);
    }

    public static TableSpec t(final String table, final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap) {
        return new TableSpec(null, null, table, fieldColumnSpecMap);
    }
}
