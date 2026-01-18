package org.litebridge.orm.persistence;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.update.Insert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        return new Insert(table.getMetaData(), rows.stream().map(DtoRowValue::rowValue).toList(), returnGeneratedKeys());
    }

    private boolean returnGeneratedKeys() {
        final Set<ColumnMetaData> autoIncrementingPks = table.getMetaData().primaryKey().stream()
                .filter(ColumnMetaData::isAutoIncrement)
                .collect(Collectors.toSet());

        if (autoIncrementingPks.isEmpty()) {
            return false;
        }

        return rows.stream()
                .flatMap(dtoRowValue -> dtoRowValue.rowValue().columns().stream())
                // Check if a value for the auto-incrementing PK was specified
                .noneMatch(columnValue -> autoIncrementingPks.contains(columnValue.column())
                        && columnValue.value() != null);
    }
}
