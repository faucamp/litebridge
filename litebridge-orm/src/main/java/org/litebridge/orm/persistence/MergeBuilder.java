package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.merge.DtoMergeOnConditionClauseTerminal;
import org.litebridge.orm.api.merge.DtoMergeOnStep;
import org.litebridge.orm.api.merge.DtoMergeUsingStep;
import org.litebridge.orm.api.merge.MergeTerminal;
import org.litebridge.orm.api.merge.MergeTerminalInspector;
import org.litebridge.orm.api.update.DtoUpdateStep;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.Fn;
import org.litebridge.orm.expression.select.AliasReferenceSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.tracking.FieldAccessor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A builder class for constructing SQL INSERT statements.
 */
final class MergeBuilder extends InsertBuilder {

    public MergeBuilder(final Object dto,
                        final OrmTable ormTable,
                        final @Nullable Class<?> contextDtoClass,
                        final LitebridgeContext litebridgeContext) {
        super(dto, ormTable, contextDtoClass, litebridgeContext);
    }

    @Override
    public Class<UpdateResult> resultType() {
        return UpdateResult.class;
    }

    @Override
    public QueryNode node() {
        if (node == null) {
            node = createMerge();
        }

        return node;
    }

    @SuppressWarnings("unchecked")
    private <DTO> QueryNode createMerge() {
        final Class<DTO> dtoClass = (Class<DTO>) ormTable.dtoClass();

        final LinkedHashMap<String, @Nullable Object> firstRow = rows.getFirst();
        final int rowCount = rows.size();
        int columnCount = firstRow.size();
        final String virtualTableAlias = "upsert" + dtoClass.getSimpleName();
        final List<String> virtualTableColumnNames = new ArrayList<>(columnCount);
        final Map<String, AliasReferenceSpec> virtualTableFields = new LinkedHashMap<>();
        final List<@Nullable Object> values = new ArrayList<>(rowCount * columnCount);

        {
            int i = 0;

            for (final LinkedHashMap<String, @Nullable Object> row : rows) {
                for (final Map.Entry<String, @Nullable Object> entry : row.sequencedEntrySet()) {
                    final String fieldName = entry.getKey();
                    final Object rawValue = entry.getValue();

                    if (rawValue instanceof LinkedHashMap<?, ?> rawValueMap) {
                        final MappedFieldTarget mappedFieldTarget = ormTable.mappedFieldTargetForField(fieldName);

                        if (mappedFieldTarget instanceof MappedCompositeKey mappedCompositeKey) {
                            final OrmTable targetOrmTable = mappedCompositeKey.targetOrmTable().get();
                            final MappedFieldTarget[] mappedCompositeKeyColumns = mappedCompositeKey.columns();
                            final String[] targetColumnNames = mappedCompositeKey.targetColumns();
                            columnCount += mappedCompositeKeyColumns.length;

                            for (int j = 0; j < mappedCompositeKeyColumns.length; j++) {
                                final ColumnMetaData keyColumn = ObjectUtils.requireInstanceOf(ColumnMetaData.class, mappedCompositeKeyColumns[j]);
                                final String columnName = keyColumn.name();
                                virtualTableColumnNames.add(columnName);
                                virtualTableFields.put(keyColumn.name(), Fn.aliasRef(virtualTableAlias, columnName));

                                final String targetColumnName = targetColumnNames[j];
                                final FieldAccessor targetField = targetOrmTable.fieldForColumnName(targetColumnName);
                                final Object columnValue = rawValueMap.get(targetField.name());
                                values.add(columnValue);
                            }

                            continue;
                        }
                    }

                    if (i < columnCount) {
                        virtualTableColumnNames.add(fieldName);
                        virtualTableFields.put(fieldName, Fn.aliasRef(virtualTableAlias, fieldName));
                    }

                    values.add(entry.getValue());
                    ++i;
                }
            }
        }

        // MERGE INTO <table> USING VALUES clause
        final DtoMergeOnStep<DTO> mergeOnStep = new DtoMergeUsingStep<>(dtoClass, contextDtoClass, litebridgeContext)
                .using(Fn.values(virtualTableAlias, virtualTableColumnNames, values));

        // Merge ON condition clause
        DtoMergeOnConditionClauseTerminal<DTO> mergeOnConditionClauseTerminal = null;
        final List<FieldAccessor> pkFields = ormTable.primaryKeyFields();

        for (FieldAccessor pkField : pkFields) {
            final String fieldName = pkField.name();

            final SelectColumnSpec pkColumn = new SelectColumnSpec(ormTable.columnMetaDataForField(fieldName).column());
            final AliasReferenceSpec aliasRef = virtualTableFields.get(fieldName);

            if (mergeOnConditionClauseTerminal == null) {
                mergeOnConditionClauseTerminal = mergeOnStep.on(pkColumn).eq(aliasRef);
            } else {
                mergeOnConditionClauseTerminal = mergeOnConditionClauseTerminal.and(pkColumn).eq(aliasRef);
            }
        }

        //noinspection DataFlowIssue
        mergeOnConditionClauseTerminal = Objects.requireNonNull(mergeOnConditionClauseTerminal);

        // Merge WHEN MATCHED UPDATE and WHEN NOT MATCHED INSERT clauses
        final MergeTerminal mergeTerminal;

        // Check if we have non-PK columns values; if so, do an UPDATE on a matching row
        if (columnCount > pkFields.size()) {
            // Update and insert
            mergeTerminal = mergeOnConditionClauseTerminal
                    .whenMatched(m -> m.update(u -> {
                        DtoUpdateStep<DTO> dtoUpdateStep = null;

                        for (Map.Entry<String, AliasReferenceSpec> entry : virtualTableFields.entrySet()) {
                            if (ormTable.isFieldPartOfPrimaryKey(entry.getKey())) {
                                // Don't update primary keys
                                continue;
                            }

                            if (dtoUpdateStep == null) {
                                dtoUpdateStep = u.set(entry.getKey()).to(entry.getValue());
                            } else {
                                dtoUpdateStep = dtoUpdateStep.set(entry.getKey()).to(entry.getValue());
                            }
                        }

                        return Objects.requireNonNull(dtoUpdateStep);
                    }))
                    .whenNotMatched(i -> i
                            .insert(virtualTableColumnNames)
                            .values(virtualTableFields.values()));
        } else {
            // No updatable columns; insert only
            mergeTerminal = mergeOnConditionClauseTerminal.whenNotMatched(i -> i.insert(virtualTableColumnNames).values(virtualTableFields.values()));
        }

        return MergeTerminalInspector.getNode(mergeTerminal);
    }
}
