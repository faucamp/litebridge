package org.litebridgedb.orm.api.select;

public interface HavingConditionClause<DTO,
        SELF extends HavingConditionClause<DTO, SELF, HCCT, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, SELF, HCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends ConditionClause<DTO, SELF, HCCT> {

}
