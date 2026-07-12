package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Update;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A builder class for constructing SQL UPDATE statements.
 * <p>
 * The {@code UpdateBuilder} is responsible for creating instances of the {@link Update} class
 * by specifying the target table, column values to update, and the conditions that determine
 * which rows are affected.
 * <p>
 * Instances of this class support method chaining for a fluent API style.
 * <p>
 * It extends {@link AbstractStatementBuilder} with {@link Update} as the specific statement type.
 */
final class UpdateBuilder extends AbstractStatementBuilder<Update> {

    @Nullable
    private List<ColumnValue> columnValues;
    private @Nullable ConditionGroup conditions;

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

    public UpdateBuilder where(final ConditionGroup conditionGroup) {
        this.conditions = conditionGroup;
        return this;
    }

    @Override
    public Update build() {
        CollectionUtils.requireNonEmpty(columnValues, () -> new IllegalArgumentException("No column values specified for UPDATE"));
        return new Update(ormTable.getMetaData().toTable(), columnValues, Objects.requireNonNull(conditions));
    }
}
