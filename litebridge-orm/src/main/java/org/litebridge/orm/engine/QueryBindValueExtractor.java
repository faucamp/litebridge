package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.ForeignKeyConstraint;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.ConditionWithIdNode;
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.InsertDtoValuesNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.engine.ast.UsingNode;
import org.litebridge.orm.engine.ast.WhenMatchedNode;
import org.litebridge.orm.engine.ast.WhenNotMatchedNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.tracking.FieldAccessor;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Extracts bind values from a query node abstract syntax tree.
 */
public final class QueryBindValueExtractor {

    private QueryBindValueExtractor() {
    }

    /**
     * Extracts and collects bind values from the provided {@link QueryNode} into a list with context.
     *
     * @param node              the root {@link QueryNode} representing the start of the query chain
     * @param litebridgeContext Litebridge context for metadata resolution
     * @return a list of bind values extracted from the query chain
     */
    public static List<@Nullable Object> extractBindValues(final QueryNode node,
                                                           final LitebridgeContext litebridgeContext) {
        final List<@Nullable Object> bindValues = new ArrayList<>();
        extractBindValues(node, bindValues, litebridgeContext);
        return bindValues;
    }

    private static void extractBindValues(final QueryNode node,
                                          final List<@Nullable Object> bindValues,
                                          final LitebridgeContext litebridgeContext) {
        final List<QueryNode> nodes = chainInSourceOrder(node);

        for (final QueryNode currentNode : nodes) {

            switch (currentNode) {
                // Select
                case JoinNode joinNode -> {
                    if (joinNode.condition() != null) {
                        extractBindValuesAtLevel(joinNode.condition(), bindValues, litebridgeContext);
                    }
                }
                case WhereNode whereNode ->
                        extractBindValuesAtLevel(whereNode.condition(), bindValues, litebridgeContext);
                case HavingNode havingNode ->
                        extractBindValuesAtLevel(havingNode.condition(), bindValues, litebridgeContext);
                case SetNode setNode -> {
                    final Object value = setNode.value();
                    bindValues.add(value);
                }
                // Insert
                case InsertValuesNode insertValuesNode -> Collections.addAll(bindValues, insertValuesNode.values());
                case InsertDtoValuesNode insertDtoValuesNode ->
                        extractDtoValues(insertDtoValuesNode, bindValues, litebridgeContext);
                // Merge
                case UsingNode usingNode -> extractBindValuesAtLevel(usingNode.on(), bindValues, litebridgeContext);
                case WhenMatchedNode whenMatchedNode -> {
                    final List<QueryNode> updateNodes = chainInSourceOrder(whenMatchedNode.update());

                    for (final QueryNode updateChild : updateNodes) {
                        if (updateChild instanceof WhereNode whereNode) {
                            extractBindValuesAtLevel(whereNode.condition(), bindValues, litebridgeContext);
                        }
                    }

                    for (final QueryNode updateChild : updateNodes) {
                        if (updateChild instanceof SetNode setNode) {
                            bindValues.add(setNode.value());
                        }
                    }
                }
                case WhenNotMatchedNode whenNotMatchedNode -> {
                    if (whenNotMatchedNode.and() != null) {
                        extractBindValuesAtLevel(whenNotMatchedNode.and(), bindValues, litebridgeContext);
                    }

                    final List<QueryNode> insertNodes = chainInSourceOrder(whenNotMatchedNode.insert());

                    for (final QueryNode insertChild : insertNodes) {
                        switch (insertChild) {
                            case InsertValuesNode insertValuesNode ->
                                    Collections.addAll(bindValues, insertValuesNode.values());
                            case InsertDtoValuesNode insertDtoValuesNode ->
                                    extractDtoValues(insertDtoValuesNode, bindValues, litebridgeContext);
                            default -> { /* Ignore */ }
                        }
                    }
                }
                default -> {
                    // Ignore other node types in main chain
                }
            }
        }
    }

    private static void extractDtoValues(final InsertDtoValuesNode insertDtoValuesNode,
                                         final List<@Nullable Object> bindValues,
                                         final LitebridgeContext litebridgeContext) {
        final Object dto = insertDtoValuesNode.dto();
        final OrmTable ormTable = litebridgeContext.tableRegistry().getOrmTable(dto.getClass());

        if (ormTable != null) {
            final List<ColumnMetaData> columnMetaDataList = ormTable.mappedColumns();

            for (final ColumnMetaData columnMetaData : columnMetaDataList) {
                final FieldAccessor fieldAccessor = ormTable.fieldForColumnNameOrNull(columnMetaData.name());

                if (fieldAccessor == null) {
                    continue;
                }

                final Object value = fieldAccessor.get(dto);

                if (value != null) {
                    final List<ForeignKeyConstraint> foreignKeyConstraints = columnMetaData.getForeignKeyConstraints();

                    if (!foreignKeyConstraints.isEmpty() && !ClassUtils.isBasicType(fieldAccessor.type())) {
                        final OrmTable fkOrmTable = litebridgeContext.tableRegistry().getOrmTableOrThrow(value.getClass());

                        for (final ForeignKeyConstraint fkc : foreignKeyConstraints) {
                            final FieldAccessor fkFieldAccessor = fkOrmTable.getFieldForColumnName(fkc.foreignKey().name());
                            final Object pkValue = fkFieldAccessor.get(value);
                            bindValues.add(pkValue);
                            break;
                        }

                        continue;
                    }
                } else {
                    if (!columnMetaData.isNullable()
                            && (columnMetaData.isAutoIncrement() || columnMetaData.getGenerator() != null)) {
                        continue;
                    }
                }

                bindValues.add(value);
            }

            return;
        }

        if (dto.getClass().isRecord()) {
            final RecordComponent[] components = dto.getClass().getRecordComponents();

            for (final RecordComponent component : components) {
                try {
                    bindValues.add(component.getAccessor().invoke(dto));
                } catch (final Exception ignored) {
                }
            }
        }
    }

    private static void extractBindValuesAtLevel(final QueryNode lastNode,
                                                 final List<@Nullable Object> bindValues,
                                                 final @Nullable LitebridgeContext litebridgeContext) {
        final List<QueryNode> nodes = chainInSourceOrder(lastNode);
        final List<ConditionGroupNode> subgroups = new ArrayList<>();

        // Process conditions before subgroups to match ConditionGroupSpec.toConditionGroup order.
        for (final QueryNode node : nodes) {
            if (node instanceof final ConditionGroupNode conditionGroupNode) {
                subgroups.add(conditionGroupNode);
            } else if (node instanceof final ConditionNode conditionNode) {
                final Operator operator = conditionNode.operator();

                if (operator == Operator.IS_NULL || operator == Operator.IS_NOT_NULL || operator == Operator.USING) {
                    continue;
                }

                final Object rhs = conditionNode.rhs();

                if (rhs instanceof SelectTerminal<?> st) {
                    extractBindValues(Objects.requireNonNull(SelectTerminalInspector.getNode(st)), bindValues, litebridgeContext);
                    continue;
                }

                if (rhs instanceof QueryNode qn) {
                    extractBindValues(qn, bindValues, litebridgeContext);
                    continue;
                }

                if (rhs instanceof Column || rhs instanceof ExpressionSpec) {
                    continue;
                }

                if (rhs instanceof Collection<?> collection) {
                    bindValues.addAll(collection);
                } else {
                    bindValues.add(rhs);
                }
            } else if (node instanceof final ConditionWithIdNode conditionWithIdNode) {
                bindValues.add(conditionWithIdNode.id());
            }
        }

        for (final ConditionGroupNode groupNode : subgroups) {
            extractBindValuesAtLevel(groupNode.lastChild(), bindValues, litebridgeContext);
        }
    }

    private static List<QueryNode> chainInSourceOrder(final QueryNode node) {
        final List<QueryNode> nodes = new ArrayList<>();
        QueryNode current = node;

        while (current != null) {
            nodes.add(current);
            current = current.previous();
        }

        for (int left = 0, right = nodes.size() - 1; left < right; left++, right--) {
            final QueryNode nodeAtLeft = nodes.get(left);
            nodes.set(left, nodes.get(right));
            nodes.set(right, nodeAtLeft);
        }

        return nodes;
    }
}
