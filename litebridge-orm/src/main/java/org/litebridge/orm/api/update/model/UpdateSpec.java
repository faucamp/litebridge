package org.litebridge.orm.api.update.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.math.MathOperation;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.orm.api.select.impl.AbstractConditionBasedSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base specification for constructing a SQL UPDATE statement.
 */
public class UpdateSpec extends AbstractConditionBasedSpec {

    private record BoundColumnValue(ColumnValue columnValue, boolean bind) {}
    protected final List<BoundColumnValue> columnValues = new ArrayList<>();

    public UpdateSpec(final Table table, final SelectExpressionMapper selectExpressionMapper) {
        super(table, selectExpressionMapper);
    }

    public void addColumnValue(final ColumnValue columnValue) {
        addColumnValue(columnValue, true);
    }

    public void addColumnValue(final ColumnValue columnValue, final boolean bind) {
        columnValues.add(new BoundColumnValue(columnValue, bind));
    }

    public PreparedOperation toUpdate(final TableMetaDataCache tableMetaDataCache, final TypeConverter typeConverter) {
        final List<BindValue> bindValues = new ArrayList<>();

        for (BoundColumnValue boundColumnValue : columnValues) {
            final ColumnValue columnValue = boundColumnValue.columnValue();
            if (boundColumnValue.bind() && !(columnValue.value() instanceof MathOperation)) {
                bindValues.addAll(createBindValues(columnValue.column(), columnValue.value(), tableMetaDataCache, typeConverter));
            }
        }

        final List<ColumnValue> updateColumnValues = columnValues.stream().map(BoundColumnValue::columnValue).toList();
        final Update update = new Update(table,
                updateColumnValues,
                conditions.toConditionGroup(selectExpressionMapper, Collections.singleton(table), bindValues, tableMetaDataCache, typeConverter));
        return new PreparedOperation(update, bindValues);
    }

    private List<BindValue> createBindValues(final Column column, final @Nullable Object rawValue, final TableMetaDataCache tableMetaDataCache, final TypeConverter typeConverter) {
        final ColumnMetaData columnMetaData = tableMetaDataCache.ensureTableMetaData(column.table()).column(column.name());

        if (rawValue instanceof Collection<?> collection) {
            return collection.stream()
                    .map(value -> typeConverter.convert(value, columnMetaData.getDataType()))
                    .map(convertedValue -> new BindValue(convertedValue, columnMetaData.getDataType()))
                    .toList();
        } else {
            final Object convertedValue = typeConverter.convert(rawValue, columnMetaData.getDataType());
            return Collections.singletonList(new BindValue(convertedValue, columnMetaData.getDataType()));
        }
    }
}
