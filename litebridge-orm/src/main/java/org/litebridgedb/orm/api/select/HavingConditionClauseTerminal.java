package org.litebridgedb.orm.api.select;

public interface HavingConditionClauseTerminal<DTO,
        HCC extends HavingConditionClause<DTO, HCC, SELF, OBC, OBCC>,
        SELF extends HavingConditionClauseTerminal<DTO, HCC, SELF, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends ConditionClauseTerminal<DTO, HCC, SELF>,
        HavingClauseTerminal<DTO, OBC, OBCC> {

}
