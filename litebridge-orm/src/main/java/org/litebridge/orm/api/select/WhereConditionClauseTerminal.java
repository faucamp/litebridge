package org.litebridge.orm.api.select;

public interface WhereConditionClauseTerminal<DTO,
        WCC extends WhereConditionClause<DTO, WCC, SELF, OBC, OBCC>,
        SELF extends WhereConditionClauseTerminal<DTO, WCC, SELF, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends ConditionClauseTerminal<DTO, WCC, SELF>,
        WhereClauseTerminal<DTO, OBC, OBCC> {

}
