package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.HavingNode;
import org.litebridge.orm.api.select.ast.JoinNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
            final Object rhs = conditionNode.rhs();

            if (rhs instanceof Column || rhs instanceof ExpressionSpec || rhs instanceof SelectTerminal || rhs instanceof QueryNode) {
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
