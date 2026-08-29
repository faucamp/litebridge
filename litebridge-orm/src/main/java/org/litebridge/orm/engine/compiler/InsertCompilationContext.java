package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.generator.ColumnValueGenerator;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.InsertV2;
import org.litebridge.db.spi.update.UpdateColumn;
import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.QueryFieldInspector;
import org.litebridge.orm.persistence.OrmTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class InsertCompilationContext implements CompilationContext {

    private final Table table;
    private final TableMetaData tableMetaData;
    private final List<ColumnMetaData> columnMetaDataList = new ArrayList<>();
    private final List<String> insertColumns;
    private int rows = 0;
    private @Nullable List<BindValue> bindValues;

    public InsertCompilationContext(final InsertNode insertNode,
                                    final LitebridgeContext litebridgeContext) {
        final OrmTable ormTable;

        if (insertNode.dtoClass() != null) {
            ormTable = litebridgeContext.tableRegistry().getOrmTableOrThrow(insertNode.dtoClass());
            this.tableMetaData = ormTable.getMetaData();
            this.table = tableMetaData.toTable();
        } else {
            this.table = litebridgeContext.tableRegistry().getOrCreateSpiTable(Objects.requireNonNull(insertNode.table()));
            this.tableMetaData = litebridgeContext.tableMetaDataCache().ensureTableMetaData(table);
            ormTable = null;
        }

        if (insertNode.columns() != null) {
            if (insertNode.columns().length > 0) {
                if (ormTable != null) {
                    // Translate field names to column names
                    insertColumns = Arrays.stream(insertNode.columns())
                            .map(ormTable::columnMetaDataForField)
                            .map(ColumnMetaData::name)
                            .toList();
                } else {
                    this.insertColumns = List.of(insertNode.columns());
                }

                // Process insert columns in the order provided
                for (final String insertColumnName : insertColumns) {
                    final ColumnMetaData columnMetaData = tableMetaData.column(insertColumnName);
                    this.columnMetaDataList.add(columnMetaData);
                }

                // Process any remaining non-nullable columns
                tableMetaData.columns().stream()
                        .filter(columnMetaData -> !insertColumns.contains(columnMetaData.name()) && !columnMetaData.isNullable())
                        .forEach(columnMetaData -> {
                            // Non-null value omitted from insert columns; see if it can be generated
                            if (columnMetaData.getGenerator() != null) {
                                // Implicit/generated value insert
                                this.columnMetaDataList.add(columnMetaData);
                            } else {
                                throw new IllegalArgumentException("NOT NULL column " + columnMetaData.name() + " omitted from insert into table " + table.qualifiedName() + ", and no value generator present");
                            }
                        });
            } else {
                // All columns
                final List<ColumnMetaData> columnMetaDatas = tableMetaData.columns();
                this.insertColumns = new ArrayList<>(columnMetaDatas.size());

                for (ColumnMetaData columnMetaData : columnMetaDatas) {
                    this.columnMetaDataList.add(columnMetaData);
                    this.insertColumns.add(columnMetaData.name());
                }
            }
        } else {
            final ExpressionSpec[] expressionSpecs = Objects.requireNonNull(insertNode.expressionSpecs());
            this.insertColumns = new ArrayList<>(expressionSpecs.length);

            for (ExpressionSpec expressionSpec : expressionSpecs) {
                if (expressionSpec instanceof ColumnExpressionSpec columnExpressionSpec) {
                    insertColumns.add(columnExpressionSpec.getColumn().name());
                } else if (expressionSpec instanceof QueryField queryField) {
                    final String fieldName = QueryFieldInspector.getFieldName(queryField);
                    final ColumnMetaData columnMetaData = ormTable.columnMetaDataForField(fieldName);
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
        final List<UpdateColumn> columns = columnMetaDataList.stream()
                .map(columnMetaData -> {
                    final ColumnValueGenerator columnValueGenerator = columnMetaData.getGenerator();

                    if (!insertColumns.contains(columnMetaData.name()) && columnValueGenerator != null) {
                        final Object generatedValue = columnValueGenerator.generate(columnMetaData);
                        return new UpdateColumn(columnMetaData.name(), generatedValue);
                    } else {
                        return new UpdateColumn(columnMetaData.name());
                    }
                })
                .toList();
        return new InsertV2(table, columns, rows, true);
    }
}
