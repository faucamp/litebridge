package org.litebridge.orm.api.insert.model;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.RowValue;
import org.litebridge.orm.api.select.impl.AbstractConditionBasedSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InsertSpec extends AbstractConditionBasedSpec {

    private final List<BoundColumnValue> columnValues = new ArrayList<>();

    public InsertSpec(final Table table, final SelectExpressionMapper selectExpressionMapper) {
        super(table, selectExpressionMapper);
    }

    public void addColumnValue(final ColumnValue columnValue, final boolean bind) {
        columnValues.add(new BoundColumnValue(columnValue, bind));
    }

    public PreparedOperation toInsert(final TableMetaDataCache tableMetaDataCache, final TypeConverter typeConverter) {
        final List<BindValue> bindValues = new ArrayList<>();
        final List<ColumnValue> insertColumnValues = new ArrayList<>();

        for (final BoundColumnValue boundColumnValue : columnValues) {
            final ColumnValue columnValue = boundColumnValue.columnValue();
            final ColumnMetaData columnMetaData = tableMetaDataCache.ensureTableMetaData(table).column(columnValue.column().name());
            final Object rawValue = columnValue.value();
            final Object convertedValue = typeConverter.convert(rawValue, columnMetaData.getDataType());

            if (boundColumnValue.bind()) {
                bindValues.add(new BindValue(convertedValue, columnMetaData.getDataType()));
            }

            insertColumnValues.add(new ColumnValue(columnValue.column(), convertedValue));
        }

        final Insert insert = new Insert(table, Collections.singletonList(new RowValue(insertColumnValues)), false);
        return new PreparedOperation(insert, bindValues);
    }

    private record BoundColumnValue(ColumnValue columnValue, boolean bind) {
    }
}
