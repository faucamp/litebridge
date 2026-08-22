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
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.QueryFieldInspector;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class InsertCompilationContext implements CompilationContext {

    private final Table table;
    private final TableMetaData tableMetaData;
    private final List<ColumnMetaData> columnMetaDataList = new ArrayList<>();
    private final Set<String> insertColumns;
    private final TypeConverter typeConverter;
    private int rows = 0;
    private @Nullable List<BindValue> bindValues;

    public InsertCompilationContext(final InsertNode insertNode,
                                    final TableRegistry tableRegistry,
                                    final TableMetaDataCache tableMetaDataCache,
                                    final TypeConverter typeConverter) {
        final OrmTable ormTable;

        if (insertNode.dtoClass() != null) {
            ormTable = tableRegistry.getTableOrThrow(insertNode.dtoClass());
            this.tableMetaData = ormTable.getMetaData();
            this.table = tableMetaData.toTable();
        } else {
            this.table = tableRegistry.getOrCreateSpiTable(Objects.requireNonNull(insertNode.table()));
            this.tableMetaData = tableMetaDataCache.ensureTableMetaData(table);
            ormTable = null;
        }

        this.typeConverter = typeConverter;

        if (insertNode.columns() != null) {
            if (insertNode.columns().length > 0) {
                if (ormTable != null) {
                    // Translate field names to column names
                    this.insertColumns = new HashSet<>(Arrays.stream(insertNode.columns())
                            .map(ormTable::getColumnForFieldName)
                            .map(ColumnMetaData::name)
                            .collect(Collectors.toSet()));
                } else {
                    this.insertColumns = new HashSet<>(Set.of(insertNode.columns()));
                }

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
            } else {
                // All columns
                final List<ColumnMetaData> columnMetaDatas = tableMetaData.columns();
                this.insertColumns = new HashSet<>(columnMetaDatas.size());

                for (ColumnMetaData columnMetaData : columnMetaDatas) {
                    this.columnMetaDataList.add(columnMetaData);
                    this.insertColumns.add(columnMetaData.name());
                }
            }
        } else {
            final ExpressionSpec[] expressionSpecs = Objects.requireNonNull(insertNode.expressionSpecs());
            this.insertColumns = new HashSet<>(expressionSpecs.length);

            for (ExpressionSpec expressionSpec : expressionSpecs) {
                if (expressionSpec instanceof ColumnExpressionSpec columnExpressionSpec) {
                    insertColumns.add(columnExpressionSpec.getColumn().name());
                } else if (expressionSpec instanceof QueryField queryField) {
                    final String fieldName = QueryFieldInspector.getFieldName(queryField);
                    final ColumnMetaData columnMetaData = ormTable.getColumnForFieldName(fieldName);
                    insertColumns.add(columnMetaData.name());
                } else {
                    throw new IllegalArgumentException("Unsupported expression spec type: " + expressionSpec.getClass().getName());
                }
            }

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
            final ColumnMetaData columnMetaData = columnMetaDataList.get(i);
            final Object value = values.get(i);

            if (value == null && !columnMetaData.isNullable()) {
                if (columnMetaData.getGenerator() != null) {
                    // Value will be generated; drop the NULL
                    this.insertColumns.remove(columnMetaData.name());
                    continue;
                } else {
                    throw new IllegalArgumentException("NULL value not allowed for non-nullable column: " + columnMetaData.name());
                }
            }

            final BindValue bindValue = new BindValue(values.get(i), columnMetaData.getDataType());
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
