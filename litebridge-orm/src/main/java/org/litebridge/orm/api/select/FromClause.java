package org.litebridge.orm.api.select;

public interface FromClause<DTO,
        FCT extends FromClauseTerminal<DTO, JC, JCC, JCCT, WCC, WCCT, OBC, OBCC>,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>,
        WCC extends WhereConditionClause<DTO, WCC, WCCT, OBC, OBCC>,
        WCCT extends WhereConditionClauseTerminal<DTO, WCC, WCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>> {

    FCT from(final String schema, final String table);

    default FCT from(final String table) {
        return from("", table);
    }
}
