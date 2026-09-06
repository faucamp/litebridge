package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.math.MathOperation;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.ConditionWithIdNode;
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class QueryBindValueExtractor {

    private QueryBindValueExtractor() {
    }

    public static List<@Nullable Object> extractBindValues(final QueryNode node) {
        final List<@Nullable Object> bindValues = new ArrayList<>();
        extractBindValues(node, bindValues);
        return bindValues;
    }

    private static void extractBindValues(final QueryNode node, final List<@Nullable Object> bindValues) {
        final List<QueryNode> nodes = chainInSourceOrder(node);

        for (final QueryNode currentNode : nodes) {

            switch (currentNode) {
            case JoinNode joinNode -> {
                if (joinNode.condition() != null) {
                    extractBindValuesAtLevel(joinNode.condition(), bindValues);
                }
            }
            case WhereNode whereNode -> extractBindValuesAtLevel(whereNode.condition(), bindValues);
            case HavingNode havingNode -> extractBindValuesAtLevel(havingNode.condition(), bindValues);
            case SetNode setNode -> {
                if (setNode.bindValue()) {
                    final Object value = setNode.value();

                    if (!(value instanceof Column) && !(value instanceof ExpressionSpec) && !(value instanceof MathOperation)) {
                        bindValues.add(value);
                    }
                }
            }
            case InsertValuesNode insertValuesNode -> Collections.addAll(bindValues, insertValuesNode.values());
            default -> {
                // Ignore other node types in main chain
            }
            }
        }
    }

    private static void extractBindValuesAtLevel(final QueryNode lastNode, final List<@Nullable Object> bindValues) {
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
                    extractBindValues(Objects.requireNonNull(SelectTerminalInspector.getNode(st)), bindValues);
                    continue;
                }

                if (rhs instanceof QueryNode qn) {
                    extractBindValues(qn, bindValues);
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
            extractBindValuesAtLevel(groupNode.lastChild(), bindValues);
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
