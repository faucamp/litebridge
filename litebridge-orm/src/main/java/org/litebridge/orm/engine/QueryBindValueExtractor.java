package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.math.MathOperation;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.HavingNode;
import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.api.select.ast.InsertValuesNode;
import org.litebridge.orm.api.select.ast.JoinNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SetNode;
import org.litebridge.orm.api.select.ast.WhereNode;
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
        final List<ConditionNode> conditions = new ArrayList<>();
        final List<ConditionGroupNode> subgroups = new ArrayList<>();

        for (QueryNode node : nodes) {
            if (node instanceof ConditionNode cn) {
                conditions.add(cn);
            } else if (node instanceof ConditionGroupNode gn) {
                subgroups.add(gn);
            }
        }

        // Process conditions first
        for (ConditionNode conditionNode : conditions) {
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
