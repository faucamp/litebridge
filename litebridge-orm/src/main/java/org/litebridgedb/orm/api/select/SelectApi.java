package org.litebridgedb.orm.api.select;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.orm.api.dto.DtoFromClauseTerminal;
import org.litebridgedb.orm.config.RelatedDtoStrategy;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.TypeOverride;

/**
 * Litebridge ORM select API.
 */
public interface SelectApi {

    /**
     * Select a registered Data Transfer Object (DTO) type for database query operations.
     * <p>
     * Shortcut method; equivalent to {@code select().from(dtoClass)}.
     *
     * @param <DTO>    The type of the DTO to select.
     * @param dtoClass The class of the DTO to be queried, which must already be registered.
     * @return A {@link DtoFromClauseTerminal} instance for querying and retrieving data for the specified DTO class.
     * @throws IllegalArgumentException if the specified DTO class is not registered in the table registry.
     */
    <DTO> DtoFromClauseTerminal<DTO> select(Class<DTO> dtoClass);

    /**
     * Select a registered Data Transfer Object (DTO) type for database query operations.
     * <p>
     * Shortcut method; equivalent to {@code select().from(dtoClass, relatedDtoStrategy)}.
     *
     * @param <DTO>    The type of the DTO to select.
     * @param dtoClass The class of the DTO to be queried, which must already be registered.
     * @return A {@link DtoFromClauseTerminal} instance for querying and retrieving data for the specified DTO class.
     * @throws IllegalArgumentException if the specified DTO class is not registered in the table registry.
     */
    <DTO> DtoFromClauseTerminal<DTO> select(Class<DTO> dtoClass, @Nullable RelatedDtoStrategy relatedDtoStrategy);

    /**
     * Select a contextually-registered Data Transfer Object (DTO) type for database query operations.
     * <p>
     * Shortcut method; equivalent to {@code select().from(dtoClass, contextDtoClass)}.
     *
     * @param <DTO>    The type of the DTO to select.
     * @param dtoClass The class of the DTO to be queried, which must already be registered.
     * @return A {@link DtoFromClauseTerminal} instance for querying and retrieving data for the specified DTO class.
     * @throws IllegalArgumentException if the specified DTO class is not registered in the table registry.
     */
    <DTO> DtoFromClauseTerminal<DTO> select(Class<DTO> dtoClass, Class<?> contextDtoClass);

    /**
     * Query data from the database, without mapping results to Data Transfer Objects (DTOs).
     * <p>
     * Creates a SQL SELECT statement with the specified fields/columns; the source table is specified
     * via a chained {@code from()} call.
     * <p>
     * This method constructs a {@link FromClauseStartTypeOverride} for further query composition
     * by specifying the target DTO or table for the query.
     *
     * @param fieldsOrColumns An array of field/lhs names to be included in the SELECT statement, dependent on
     *                        whether a DTO or raw SQL is selected in the chained {@code from()} call.
     *                        Each field/lhs name must be a valid, non-null string.
     * @return A {@link FromClauseStartTypeOverride} instance allowing further refinement of the SQL query by specifying the target DTO or table.
     */
    FromClauseStart select(String... fieldsOrColumns);

    /**
     * Query data from the database, without mapping results to Data Transfer Objects (DTOs).
     * <p>
     * Creates a SQL SELECT statement with the specified fields/columns; the source table is specified
     * via a chained {@code from()} call.
     * <p>
     * This method constructs a {@link FromClauseStartTypeOverride} for further query composition
     * by specifying the target DTO or table for the query.
     *
     * @param expressions An array of {@link ExpressionSpec} objects representing the expressions
     *                    to be part of the SELECT statement.
     * @return A {@link FromClauseStartTypeOverride} instance allowing further refinement of the SQL query by specifying the target DTO or table.
     */
    FromClauseStart select(ExpressionSpec... expressions);

    /**
     * Query data from the database, without mapping results to Data Transfer Objects (DTOs).
     * <p>
     * Creates a SQL SELECT statement with the specified fields/columns; the source table is specified
     * via a chained {@code from()} call.
     * <p>
     * This method constructs a {@link FromClauseStartTypeOverride} for further query composition
     * by specifying the target DTO or table for the query.
     *
     * @param <T>        The return type of the query
     * @param expression Return type-overriding expression
     * @return A {@link FromClauseStartTypeOverride} instance allowing further refinement of the SQL query by specifying the target DTO or table.
     */
    <T> FromClauseStartTypeOverride<T> select(TypeOverride<T> expression);

    /**
     * Query data from the database, without mapping results to Data Transfer Object (DTOs).
     * <p>
     * Creates a SQL SELECT statement with all fields/columns. The source table is specified
     * via a chained {@code from()} call.
     * <p>
     * This method constructs a {@link FromClauseStartTypeOverride} for further query composition
     * by specifying the target DTO or table for the query.
     *
     * @return A {@link FromClauseStartTypeOverride} instance allowing further refinement of the SQL query by specifying the target DTO or table.
     */
    FromClauseStart select();
}
