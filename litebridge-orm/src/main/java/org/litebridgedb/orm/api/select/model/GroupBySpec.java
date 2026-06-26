package org.litebridgedb.orm.api.select.model;

/**
 * Specification for a "GROUP BY" clause in a database query.
 *
 * @param columns The lhs(s) to group by.
 */
public record GroupBySpec(String[] columns) {
}
