package org.litebridge.orm.api.select;

public interface JoinClauseTerminal<DTO,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>>

        extends WhereClauseTerminal<DTO> {

    WhereConditionClause<DTO> where(final String column);

    JC join(String table);

}
