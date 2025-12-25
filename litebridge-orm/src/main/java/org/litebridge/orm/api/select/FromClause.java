package org.litebridge.orm.api.select;

public interface FromClause<DTO,
        FCT extends FromClauseTerminal<DTO, JC, JCC, JCCT, WCC, WCCT>,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>,
        WCC extends WhereConditionClause<DTO, WCC, WCCT>,
        WCCT extends WhereConditionClauseTerminal<DTO, WCC, WCCT>> {

    FCT from(final String schema, final String table);

    FCT from(final String table);
}
