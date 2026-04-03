package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class UpdateBuilder extends AbstractStatementBuilder<Update> {

    @Nullable
    private List<ColumnValue> columnValues;
    private final List<Condition> conditions = new ArrayList<>();

    public UpdateBuilder(final OrmTable table) {
        super(table);
    }

    public List<ColumnValue> getColumnValues() {
        return columnValues != null ? columnValues : Collections.emptyList();
    }

    public UpdateBuilder setColumnValues(final List<ColumnValue> columnValues) {
        this.columnValues = columnValues;
        return this;
    }

    public UpdateBuilder where(final Condition condition) {
        conditions.add(condition);
        return this;
    }

    @Override
    public Update build() {
        CollectionUtils.requireNonEmpty(columnValues, () -> new IllegalArgumentException("No column values specified for UPDATE"));
        return new Update(ormTable.getMetaData().toTable(), columnValues, conditions);
    }
}
