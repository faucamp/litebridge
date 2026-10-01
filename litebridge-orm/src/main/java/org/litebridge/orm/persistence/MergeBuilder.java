package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.merge.DtoMergeInsertStep;
import org.litebridge.orm.api.merge.DtoMergeOnStep;
import org.litebridge.orm.api.merge.DtoMergeUpdateStep;
import org.litebridge.orm.api.merge.DtoMergeUsingStep;
import org.litebridge.orm.api.merge.MergeOnConditionClauseTerminal;
import org.litebridge.orm.api.merge.MergeTerminal;
import org.litebridge.orm.api.merge.MergeTerminalInspector;
import org.litebridge.orm.api.update.DtoUpdateStep;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;
import org.litebridge.orm.expression.select.AliasReferenceSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.tracking.FieldAccessor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A builder class for constructing SQL INSERT statements.
 */
final class MergeBuilder extends InsertBuilder {

    private final Object dto;


    public MergeBuilder(final Object dto,
                        final OrmTable table,
                        final @Nullable Class<?> contextDtoClass,
                        final LitebridgeContext litebridgeContext) {
        super(table, contextDtoClass, litebridgeContext);
        this.dto = dto;
    }

    @Override
    public Class<UpdateResult> resultType() {
        return UpdateResult.class;
    }

    @Override
    public QueryNode node() {
        if (node == null) {
            node = createMerge(rows.getFirst());
        }

        return node;
    }

    private QueryNode createMerge(final LinkedHashMap<String, @Nullable Object> row) {
        final Class<?> dtoClass = ormTable.dtoClass();
        final String tableAlias = "upsert" + dtoClass.getSimpleName();
        final List<FieldAccessor> pkFields = ormTable.getPrimaryKeyFields();
        final LinkedHashMap<String, @Nullable Object> pkValues = new LinkedHashMap<>(pkFields.size());

        for (FieldAccessor pkField : pkFields) {
            final String fieldName = pkField.name();
            pkValues.put(fieldName, row.get(fieldName));
        }

        final int columnCount = row.size();
        final String[] fieldNames = new String[columnCount];
        final @Nullable Object[] values = new Object[columnCount];
        final ExpressionSpec[] literalExpressions = new ExpressionSpec[columnCount];

        {
            int i = 0;

            for (Map.Entry<String, @Nullable Object> entry : row.sequencedEntrySet()) {
                final String fieldName = entry.getKey();
                final Object value = entry.getValue();

                fieldNames[i] = fieldName;
                values[i] = value;
                literalExpressions[i] = Fn.literal(value, fieldName);
                i++;
            }
        }

        final DtoMergeOnStep<?> mergeOnStep;
        final boolean mergeUsingValuesSupported = litebridgeContext.databaseProvider().metaData()
                .mergeCapability() == DatabaseProviderMetaData.MergeCapability.USING_VALUES;

        if (mergeUsingValuesSupported) {
            // Merge using VALUES clause
            mergeOnStep = new DtoMergeUsingStep<>(dtoClass, contextDtoClass, litebridgeContext)
                    .using(Fn.values(tableAlias, pkValues));
        } else {
            // Merge using selecting literals
            mergeOnStep = new DtoMergeUsingStep<>(dtoClass, contextDtoClass, litebridgeContext)
                    .using(Fn.alias(q -> q.select(literalExpressions).from(dtoClass),
                            tableAlias));
        }

        MergeOnConditionClauseTerminal<?, DtoMergeUpdateStep<?>, DtoMergeInsertStep> mergeOnConditionClauseTerminal = null;

        for (FieldAccessor pkField : pkFields) {
            final SelectColumnSpec pkColumn = new SelectColumnSpec(ormTable.columnMetaDataForField(pkField).column());
            final AliasReferenceSpec aliasRef = Fn.aliasRef(tableAlias, pkField.name());
            mergeOnConditionClauseTerminal = (MergeOnConditionClauseTerminal) mergeOnStep.on(pkColumn).eq(aliasRef);
        }

        final MergeTerminal mergeTerminal = Objects.requireNonNull(mergeOnConditionClauseTerminal)
                .whenMatched(m -> m.update(u -> {
                    DtoUpdateStep<?> dtoUpdateStep = null;

                    for (Map.Entry<String, @Nullable Object> entry : row.sequencedEntrySet()) {
                        if (dtoUpdateStep == null) {
                            dtoUpdateStep = u.set(entry.getKey()).to(entry.getValue());
                        } else {
                            dtoUpdateStep = dtoUpdateStep.set(entry.getKey()).to(entry.getValue());
                        }
                    }

                    return Objects.requireNonNull(dtoUpdateStep);
                }))
                .whenNotMatched(i -> i.insert(fieldNames).values(values));

        return MergeTerminalInspector.getNode(mergeTerminal);
    }
}
