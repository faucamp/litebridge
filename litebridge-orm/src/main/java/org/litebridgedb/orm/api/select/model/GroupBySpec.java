package org.litebridgedb.orm.api.select.model;

import org.litebridgedb.orm.expression.ExpressionSpec;

import java.util.Arrays;
import java.util.List;

/**
 * Specification for a "GROUP BY" clause in a database query.
 *
 * @param expressions The expressions to group by.
 */
public record GroupBySpec(List<ExpressionSpec> expressions) {

    public GroupBySpec(ExpressionSpec[] expressions) {
        this(Arrays.stream(expressions).toList());
    }
}
