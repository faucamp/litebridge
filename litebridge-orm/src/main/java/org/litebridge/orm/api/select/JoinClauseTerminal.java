package org.litebridge.orm.api.select;

public interface JoinClauseTerminal<DTO,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>,
        WCC extends WhereConditionClause<DTO, WCC, WCCT>,
        WCCT extends WhereConditionClauseTerminal<DTO, WCC, WCCT>>

        extends WhereClauseTerminal<DTO> {

    WCC where(final String column);

    JC join(String table);

}
