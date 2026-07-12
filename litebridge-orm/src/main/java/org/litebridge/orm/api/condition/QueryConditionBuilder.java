package org.litebridge.orm.api.condition;

import java.util.function.Function;

/**
 * Functional interface for building query conditions.
 *
 * @param <DTO> the type of the DTO being queried
 */
@FunctionalInterface
public interface QueryConditionBuilder<DTO> extends Function<AbstractConditionClauseStart<DTO>, AbstractCbConditionClauseTerminal<DTO>> {
}
