package org.litebridge.orm.api.condition;

import java.util.function.Function;

@FunctionalInterface
public interface QueryConditionBuilder<DTO> extends Function<AbstractConditionClauseStart<DTO>, AbstractCbConditionClauseTerminal<DTO>> {
}
