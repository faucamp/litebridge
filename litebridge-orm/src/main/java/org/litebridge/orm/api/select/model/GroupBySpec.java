package org.litebridge.orm.api.select.model;

import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Arrays;
import java.util.List;

/**
 * Specification for a "GROUP BY" clause in a database query.
 *
 * @param expressions The expressions to group by.
 */
public record GroupBySpec(List<ExpressionSpec> expressions) {

    /**
     * Constructs a new {@code GroupBySpec} from an array of expression specifications.
     *
     * @param expressions the expressions to group by.
     */
    public GroupBySpec(ExpressionSpec[] expressions) {
        this(Arrays.stream(expressions).toList());
    }
}
