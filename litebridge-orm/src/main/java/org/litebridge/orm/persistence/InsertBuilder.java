package org.litebridge.orm.persistence;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.update.Insert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A builder class for constructing SQL INSERT statements.
 * <p>
 * The {@code InsertBuilder} is responsible for creating instances of the {@link Insert} class
 * by specifying the target table, column values to insert, and whether to return generated keys.
 * <p>
 * Instances of this class support method chaining for a fluent API style.
 */
final class InsertBuilder extends AbstractStatementBuilder<Insert> {

    private final List<DtoRowValue> rows = new ArrayList<>();

    public InsertBuilder(final OrmTable table) {
        super(table);
    }

    public InsertBuilder add(final DtoRowValue dtoRowValue) {
        rows.add(dtoRowValue);
        return this;
    }

    @Override
    public Insert build() {
        return new Insert(ormTable.getMetaData().toTable(), rows.stream().map(DtoRowValue::rowValue).toList(), returnGeneratedKeys());
    }

    private boolean returnGeneratedKeys() {
        final Set<String> autoIncrementingPks = ormTable.getMetaData().primaryKey().stream()
                .filter(columnMetadata -> columnMetadata.isAutoIncrement()
                        || (columnMetadata.getGenerator() != null && SequenceColumnValueGenerator.class.isAssignableFrom(columnMetadata.getGenerator().getClass())))
                .map(ColumnMetaData::name)
                .collect(Collectors.toSet());

        if (autoIncrementingPks.isEmpty()) {
            return false;
        }

        return rows.stream()
                .flatMap(dtoRowValue -> dtoRowValue.rowValue().columns().stream())
                // Check if a value for the auto-incrementing PK was specified
                .noneMatch(columnValue -> autoIncrementingPks.contains(columnValue.column().name())
                        && columnValue.value() != null);
    }
}
