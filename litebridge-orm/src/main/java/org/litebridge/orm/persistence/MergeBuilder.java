package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
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

    public MergeBuilder(final Object dto, final OrmTable table, final @Nullable Class<?> contextDtoClass, final LitebridgeContext litebridgeContext) {
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
            node = createMerge();
        }

        return node;
    }

    private QueryNode createMerge() {
        final LinkedHashMap<String, @Nullable Object> firstRow = rows.getFirst();
        final int columnCount = firstRow.size();
        final String[] fieldNames = new String[columnCount];
        final @Nullable Object[] values = new Object[columnCount];
        final ExpressionSpec[] literalExpressions = new ExpressionSpec[columnCount];

        {
            int i = 0;

            for (Map.Entry<String, @Nullable Object> entry : firstRow.sequencedEntrySet()) {
                final String fieldName = entry.getKey();
                final Object value = entry.getValue();

                fieldNames[i] = fieldName;
                values[i] = value;
                literalExpressions[i] = Fn.literal(value, fieldName);
                i++;
            }
        }

        final Class<?> dtoClass = ormTable.dtoClass();
        final List<FieldAccessor> pkFields = ormTable.getPrimaryKeyFields();

        final DtoMergeOnStep<?> mergeOnStep = new DtoMergeUsingStep<>(dtoClass, litebridgeContext)
                .using(Fn.alias(q -> q.select(literalExpressions).from(dtoClass),
                        "lb_new_record"));

        MergeOnConditionClauseTerminal<?, DtoMergeUpdateStep<?>, DtoMergeInsertStep> mergeOnConditionClauseTerminal = null;

        for (FieldAccessor pkField : pkFields) {
            final SelectColumnSpec pkColumn = new SelectColumnSpec(ormTable.columnMetaDataForField(pkField).column());
            final Object pkValue = pkField.get(dto);
            mergeOnConditionClauseTerminal = (MergeOnConditionClauseTerminal) mergeOnStep.on(pkColumn).eq(pkValue);
        }

        final MergeTerminal mergeTerminal = Objects.requireNonNull(mergeOnConditionClauseTerminal)
                .whenMatched(m -> m.update(u -> {
                    DtoUpdateStep<?> dtoUpdateStep = null;

                    for (Map.Entry<String, @Nullable Object> entry : firstRow.sequencedEntrySet()) {
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
