package org.litebridge.orm.api.select;

public interface JoinClause<DTO> {

    JoinConditionClause<DTO> on(String column);

}
