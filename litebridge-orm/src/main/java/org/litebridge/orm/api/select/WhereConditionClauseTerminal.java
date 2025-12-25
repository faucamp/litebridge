package org.litebridge.orm.api.select;

public interface WhereConditionClauseTerminal<DTO,
        WCC extends WhereConditionClause<DTO, WCC, SELF>,
        SELF extends WhereConditionClauseTerminal<DTO, WCC, SELF>>

        extends ConditionClauseTerminal<DTO, WCC, SELF>, WhereClauseTerminal<DTO> {

}
