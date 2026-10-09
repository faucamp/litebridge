package org.litebridge.orm.api.condition;

/**
 * Functional interface for building DTO-mode query conditions.
 *
 * @param <DTO> the type of the DTO being queried
 */
@FunctionalInterface
public interface DtoQueryConditionBuilder<DTO> extends QueryConditionBuilder<DTO,
        DtoConditionClauseStart<DTO>,
        CbDtoConditionClause<DTO>,
        CbDtoConditionClauseTerminal<DTO>,
        DtoQueryConditionBuilder<DTO>> {
}
