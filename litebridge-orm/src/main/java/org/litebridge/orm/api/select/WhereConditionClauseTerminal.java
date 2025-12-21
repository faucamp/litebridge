package org.litebridge.orm.api.select;

public interface WhereConditionClauseTerminal<DTO> extends
        ConditionClauseTerminal<DTO, WhereConditionClauseTerminal<DTO>>,
        SelectTerminal<DTO> {

    //FNA: begin orderBy
    OrderByClause<DTO> orderBy(String column);
}
