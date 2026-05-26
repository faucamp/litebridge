package org.litebridgedb.orm.api.register;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.generator.ColumnValueGenerator;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.orm.api.spec.ColumnMapping;
import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.spec.FieldMapping;
import org.litebridgedb.orm.api.spec.TableMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class RegistrationContextTerminal {

    final Class<?> dtoClass;
    final String tableName;
    final Map<FieldMapping, ColumnMapping> fieldColumnMap;
    final DatabaseProvider databaseProvider;
    final @Nullable List<Class<?>> dtoInterfaces;

    RegistrationContextTerminal(final Class<?> dtoClass, final String tableName, final DatabaseProvider databaseProvider, @Nullable final List<Class<?>> dtoInterfaces) {
        this.dtoClass = dtoClass;
        this.tableName = tableName;
        this.databaseProvider = databaseProvider;
        this.fieldColumnMap = new LinkedHashMap<>();
        this.dtoInterfaces = dtoInterfaces;
    }

    public RegistrationContextTerminal with(final Function<FieldColumnSpecBuilder, FieldColumnSpecBuilderTerminal> spec) {
        final FieldColumnSpec fieldColumnSpec = FieldColumnSpecBuilder.spec(spec);

        // Override the placeholder sequence generator with the real one if needed
        if (!(databaseProvider instanceof PlaceHolderDatabaseProvider)
                && fieldColumnSpec.column() instanceof ColumnSpec(
                String name,
                ColumnValueGenerator generator,
                String joinColumn,
                TableMapping mappedTable
        ) && generator instanceof PlaceholderSequenceColumnValueGenerator placeholderGenerator) {
            final SequenceColumnValueGenerator resolvedSequenceGenerator = databaseProvider.getSequenceColumnValueGenerator(placeholderGenerator.sequence());
            final ColumnSpec resolvedColumnSpec = new ColumnSpec(name, resolvedSequenceGenerator, joinColumn, mappedTable);
            this.fieldColumnMap.put(fieldColumnSpec.field(), resolvedColumnSpec);
        } else {
            this.fieldColumnMap.put(fieldColumnSpec.field(), fieldColumnSpec.column());
        }

        return this;
    }
}
