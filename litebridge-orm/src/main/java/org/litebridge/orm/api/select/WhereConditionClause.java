package org.litebridge.orm.api.select;

public interface WhereConditionClause<DTO,
        SELF extends WhereConditionClause<DTO, SELF, WCCT>,
        WCCT extends WhereConditionClauseTerminal<DTO, SELF, WCCT>>

        extends ConditionClause<DTO, SELF, WCCT> {

}
