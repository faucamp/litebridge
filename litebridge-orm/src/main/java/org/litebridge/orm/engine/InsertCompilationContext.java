package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.generator.ColumnValueGenerator;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.InsertV2;
import org.litebridge.orm.api.select.ast.InsertNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class InsertCompilationContext implements CompilationContext {

    private final Table table;
    private final List<ColumnMetaData> columnMetaDataList = new ArrayList<>();
    private final Set<String> insertColumns;
    private final TypeConverter typeConverter;
    private int rows = 0;
    private @Nullable List<BindValue> bindValues;

    public InsertCompilationContext(final InsertNode insertNode,
                                    final TableMetaData tableMetaData,
                                    final TypeConverter typeConverter) {
        this.table = insertNode.table();
        this.typeConverter = typeConverter;
        this.insertColumns = Set.of(insertNode.columns());
        final Set<String> unmappedInsertColumns = new HashSet<>(insertColumns);

        for (ColumnMetaData columnMetaData : tableMetaData.columns()) {
            if (insertColumns.contains(columnMetaData.name())) {
                // Explicit insert
                this.columnMetaDataList.add(columnMetaData);
                unmappedInsertColumns.remove(columnMetaData.name());
            } else if (!columnMetaData.isNullable()) {
                // Non-null value omitted from insert columns; see if it can be generated
                if (columnMetaData.getGenerator() != null) {
                    // Implicit/generated value insert
                    this.columnMetaDataList.add(columnMetaData);
                } else {
                    throw new IllegalArgumentException("NOT NULL column " + columnMetaData.name() + " omitted from insert into table " + table.qualifiedName() + ", and no value generator present");
                }
            }
        }

        if (!unmappedInsertColumns.isEmpty()) {
            throw new IllegalArgumentException("Invalid insert column(s) for table " + table.qualifiedName() + ": " + unmappedInsertColumns);
        }
    }

    public void addRowBindValues(final List<@Nullable Object> values) {
        if (values.size() != insertColumns.size()) {
            throw new IllegalArgumentException("Number of values does not match number of columns");
        }

        ++rows;

        if (this.bindValues == null) {
            this.bindValues = new ArrayList<>(values.size());
        }

        for (int i = 0; i < values.size(); i++) {
            final BindValue bindValue = new BindValue(values.get(i), columnMetaDataList.get(i).getDataType());
            this.bindValues.add(bindValue);
        }
    }

    @Override
    public List<BindValue> getBindValues() {
        return bindValues != null ? bindValues : Collections.emptyList();
    }

    @Override
    public InsertV2 toOperation() {
        final List<InsertV2.InsertColumn> columns = columnMetaDataList.stream()
                .map(columnMetaData -> {
                    final ColumnValueGenerator columnValueGenerator = columnMetaData.getGenerator();

                    if (!insertColumns.contains(columnMetaData.name()) && columnValueGenerator != null) {
                        final Object generatedValue = columnValueGenerator.generate(columnMetaData);
                        return new InsertV2.InsertColumn(columnMetaData.name(), generatedValue);
                    } else {
                        return new InsertV2.InsertColumn(columnMetaData.name(), null);
                    }
                })
                .toList();
        return new InsertV2(table, columns, rows, true);
    }
}
