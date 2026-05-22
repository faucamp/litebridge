package org.litebridgedb.orm.api.register;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.orm.api.spec.ColumnMapping;
import org.litebridgedb.orm.api.spec.DtoTableSpec;
import org.litebridgedb.orm.api.spec.FieldMapping;
import org.litebridgedb.orm.api.spec.FieldSpec;
import org.litebridgedb.orm.api.spec.TableSpec;

import javax.xml.crypto.Data;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public sealed class RegistrationTableContextImpl implements RegistrationTableContext, DtoTableSpecBuilder
        permits RegistrationColumnContext, RegistrationJoinStep {

    private final String tableName;
    private final Map<FieldMapping, ColumnMapping> fieldColumnMap;
    private final DatabaseProvider databaseProvider;
    private final @Nullable List<Class<?>> dtoInterfaces;

    RegistrationTableContextImpl(final String tableName, final DatabaseProvider databaseProvider, @Nullable final List<Class<?>> dtoInterfaces) {
        this.tableName = tableName;
        this.databaseProvider = databaseProvider;
        this.fieldColumnMap = new LinkedHashMap<>();
        this.dtoInterfaces = dtoInterfaces;
    }

    RegistrationTableContextImpl(final RegistrationTableContextImpl other) {
        this.tableName = other.tableName;
        this.databaseProvider = other.databaseProvider;
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
    public DtoTableSpec buildDtoTableSpec(final Class<?> dtoClass) {
        return new DtoTableSpec(dtoClass, new TableSpec(tableName, fieldColumnMap), dtoInterfaces != null ? dtoInterfaces : Collections.emptyList());
    }

    void addFieldColumnMapping(final FieldMapping fieldMapping, final ColumnMapping columnMapping) {
        this.fieldColumnMap.put(fieldMapping, columnMapping);
    }

    DatabaseProvider databaseProvider() {
        return databaseProvider;
    }
}
