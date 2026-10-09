package org.litebridge.orm.api.condition;

import org.litebridge.orm.api.select.ConditionClause;
import org.litebridge.orm.api.select.ConditionClauseTerminal;

import java.util.function.Function;

/**
 * Functional interface for building query conditions.
 *
 * @param <DTO> the type of the DTO being queried
 */
@FunctionalInterface
public interface QueryConditionBuilder<DTO,
        CCS extends AbstractConditionClauseStart<DTO, CC, CCT, SELF>,
        CC extends ConditionClause<DTO, CC, CCT, SELF>,
        CCT extends ConditionClauseTerminal<DTO, CC, CCT, SELF>,
        SELF extends QueryConditionBuilder<DTO, CCS, CC, CCT, SELF>>

        extends Function<CCS, CCT> {
}
