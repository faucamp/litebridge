package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.HavingNode;
import org.litebridge.orm.api.select.ast.JoinNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;

import java.util.ArrayList;
import java.util.Collection;
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
        switch (node) {
            case JoinNode joinNode -> extractBindValues(joinNode.condition(), bindValues);
            case WhereNode whereNode -> extractBindValues(whereNode.condition(), bindValues);
            case HavingNode havingNode -> extractBindValues(havingNode.condition(), bindValues);
            case ConditionNode conditionNode -> {
                if (conditionNode.rhs() instanceof Collection<?> collection) {
                    bindValues.addAll(collection);
                } else {
                    bindValues.add(conditionNode.rhs());
                }
            }
            default -> {
                // Ignore
            }
        }

        final QueryNode prevNode = node.previous();

        if (prevNode != null) {
            extractBindValues(prevNode, bindValues);
        }
    }
}
