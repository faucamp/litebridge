package org.litebridge.orm.api.select;

public interface WhereConditionClause<DTO,
        SELF extends WhereConditionClause<DTO, SELF, WCCT, OBC, OBCC>,
        WCCT extends WhereConditionClauseTerminal<DTO, SELF, WCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends ConditionClause<DTO, SELF, WCCT> {

}
