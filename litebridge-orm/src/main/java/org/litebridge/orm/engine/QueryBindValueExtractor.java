package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.math.MathOperation;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.ConditionQueryNode;
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
        final QueryNode prevNode = node.previous();

        if (prevNode != null) {
            extractBindValues(prevNode, bindValues);
        }

        switch (node) {
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
            case InsertValuesNode insertValuesNode -> {
                for (final Object value : insertValuesNode.values()) {
                    bindValues.add(value);
                }
            }
            default -> {
                // Ignore other node types in main chain
            }
        }
    }

    private static void extractBindValuesAtLevel(final QueryNode lastNode, final List<@Nullable Object> bindValues) {
        final List<QueryNode> nodes = flatten(lastNode);

        // Separate conditions and subgroups to match ConditionGroupSpec.toConditionGroup order
        final List<ConditionQueryNode> conditions = new ArrayList<>();
        final List<ConditionGroupNode> subgroups = new ArrayList<>();

        for (QueryNode node : nodes) {
            if (node instanceof ConditionGroupNode conditionGroupNode) {
                subgroups.add(conditionGroupNode);
            } else if (node instanceof ConditionQueryNode conditionQueryNode) {
                conditions.add(conditionQueryNode);
            }
        }

        // Process conditions first
        for (ConditionQueryNode conditionQueryNode : conditions) {
            if (conditionQueryNode instanceof ConditionNode conditionNode) {
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
            } else if (conditionQueryNode instanceof ConditionWithIdNode conditionWithIdNode) {
                bindValues.add(conditionWithIdNode.id());
            }
        }

        // Then process subgroups
        for (ConditionGroupNode groupNode : subgroups) {
            extractBindValuesAtLevel(groupNode.lastChild(), bindValues);
        }
    }

    private static List<QueryNode> flatten(final QueryNode node) {
        final List<QueryNode> nodes = new ArrayList<>();
        QueryNode current = node;

        while (current != null) {
            nodes.add(current);
            current = current.previous();
        }

        Collections.reverse(nodes);
        return nodes;
    }
}
