package org.litebridge.orm.api.select;

public interface FromClauseTerminal<DTO,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>,
        WCC extends WhereConditionClause<DTO, WCC, WCCT>,
        WCCT extends WhereConditionClauseTerminal<DTO, WCC, WCCT>>

        extends JoinClauseTerminal<DTO, JC, JCC, JCCT, WCC, WCCT> {

}
