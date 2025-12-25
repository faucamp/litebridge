package org.litebridge.orm.api.select;

public interface JoinConditionClause<DTO,
        SELF extends JoinConditionClause<DTO, SELF, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, SELF, JCCT>>

        extends ConditionClause<DTO, SELF, JCCT> {

}
