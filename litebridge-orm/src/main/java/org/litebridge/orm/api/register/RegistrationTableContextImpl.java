package org.litebridge.orm.api.register;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.FieldMapping;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.TableSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public sealed class RegistrationTableContextImpl implements RegistrationTableContext, RegistrationSpec
        permits RegistrationColumnContext, RegistrationJoinStep {

    private final String tableName;
    private final Map<FieldMapping, ColumnMapping> fieldColumnMap;
    private final @Nullable List<Class<?>> dtoInterfaces;

    RegistrationTableContextImpl(final String tableName, @Nullable final List<Class<?>> dtoInterfaces) {
        this.tableName = tableName;
        this.fieldColumnMap = new LinkedHashMap<>();
        this.dtoInterfaces = dtoInterfaces;
    }

    RegistrationTableContextImpl(final RegistrationTableContextImpl other) {
        this.tableName = other.tableName;
        this.fieldColumnMap = other.fieldColumnMap;
        this.dtoInterfaces = other.dtoInterfaces;
    }

    @Override
    public RegistrationFieldContext mapField(final String fieldName) {
        return new RegistrationFieldContext(new FieldSpec(fieldName, false), this);
    }

    @Override
    public RegistrationFieldContext mapProperty(final String fieldName) {
        return new RegistrationFieldContext(new FieldSpec(fieldName, true), this);
    }

    @Override
    public TableSpec buildTableSpec() {
        return new TableSpec(tableName, fieldColumnMap);
    }

    @Override
    public @Nullable List<Class<?>> dtoInterfaces() {
        return dtoInterfaces;
    }

    void addFieldColumnMapping(final FieldMapping fieldMapping, final ColumnMapping columnMapping) {
        this.fieldColumnMap.put(fieldMapping, columnMapping);
    }
}
