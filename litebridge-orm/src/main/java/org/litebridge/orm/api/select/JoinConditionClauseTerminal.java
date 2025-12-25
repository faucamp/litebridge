package org.litebridge.orm.api.select;

public interface JoinConditionClauseTerminal<DTO,
        JCC extends JoinConditionClause<DTO, JCC, SELF>,
        SELF extends JoinConditionClauseTerminal<DTO, JCC, SELF>>

        extends ConditionClauseTerminal<DTO, JCC, SELF> {

}
