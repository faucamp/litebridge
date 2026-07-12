package org.litebridge.orm.api.select;

/**
 * A terminal condition clause for a {@code HAVING} clause in a SQL query.
 *
 * @param <DTO>  The DTO type.
 * @param <HCC>  The HavingConditionClause type.
 * @param <SELF> The type of the clause itself.
 * @param <OBC>  The OrderByClause type.
 * @param <OBCC> The OrderByClauseChain type.
 */
public interface HavingConditionClauseTerminal<DTO,
        HCC extends HavingConditionClause<DTO, HCC, SELF, OBC, OBCC>,
        SELF extends HavingConditionClauseTerminal<DTO, HCC, SELF, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends ConditionClauseTerminal<DTO, HCC, SELF>,
        HavingClauseTerminal<DTO, OBC, OBCC> {

}
