package org.litebridge.orm.api.select;

public interface FromClause<DTO,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>,
        FCT extends FromClauseTerminal<DTO, JC, JCC, JCCT>> {

    FCT from(final String schema, final String table);

    FCT from(final String table);
}
