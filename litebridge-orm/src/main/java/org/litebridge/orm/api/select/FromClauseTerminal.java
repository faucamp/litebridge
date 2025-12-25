package org.litebridge.orm.api.select;

public interface FromClauseTerminal<DTO,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>>

        extends JoinClauseTerminal<DTO, JC, JCC, JCCT> {

}
