package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

/**
 * Represents a USING clause in a MERGE statement in the query AST.
 *
 * @param previous The root {@link MergeNode} for the MERGE INTO statement
 * @param table    The name of the table to merge into
 * @param dtoClass The DTO class to merge into
 * @param on       AST nodes representing the condition(s) to use for matching rows
 */
public record UsingNode(MergeNode previous,
                        @Nullable String table,
                        @Nullable Class<?> dtoClass,
                        QueryNode on) implements QueryNode {
}
