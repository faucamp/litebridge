package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.config.RelatedDtoStrategy;

/**
 * Represents a FROM clause in the query AST.
 *
 * @param previous           the previous node in the chain
 * @param dtoClass           the DTO class to select from
 * @param contextDtoClass    the context DTO class
 * @param tableName          the table name to select from (for SQL-based queries)
 * @param relatedDtoStrategy the strategy for fetching related DTOs
 */
public record FromNode(@Nullable QueryNode previous,
                       @Nullable Class<?> dtoClass,
                       @Nullable Class<?> contextDtoClass,
                       @Nullable String tableName,
                       @Nullable RelatedDtoStrategy relatedDtoStrategy) implements QueryNode {
}
