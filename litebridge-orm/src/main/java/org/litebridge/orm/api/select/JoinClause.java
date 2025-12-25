package org.litebridge.orm.api.select;

public interface JoinClause<DTO,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>> {

    JCC on(String column);

}
