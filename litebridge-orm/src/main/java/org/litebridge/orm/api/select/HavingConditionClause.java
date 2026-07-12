package org.litebridge.orm.api.select;

/**
 * A condition clause for a {@code HAVING} clause in a SQL query.
 *
 * @param <DTO>  The DTO type.
 * @param <SELF> The type of the clause itself.
 * @param <HCCT> The HavingConditionClauseTerminal type.
 * @param <OBC>  The OrderByClause type.
 * @param <OBCC> The OrderByClauseChain type.
 */
public interface HavingConditionClause<DTO,
        SELF extends HavingConditionClause<DTO, SELF, HCCT, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, SELF, HCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends ConditionClause<DTO, SELF, HCCT> {

}
