package org.litebridge.orm.api.select.dto;

import org.litebridge.orm.api.select.FromClauseTerminal;
import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.impl.DelegatingSelectTerminal;
import org.litebridge.orm.api.select.impl.SelectSpec;

import java.util.Map;

public class DtoFromClauseTerminal<DTO> extends DelegatingSelectTerminal<DTO> implements FromClauseTerminal<DTO> {

    private final SelectSpec selectSpec;

    public DtoFromClauseTerminal(final SelectSpec selectSpec, final SelectTerminal<DTO> selectTerminal) {
        super(selectTerminal);
        this.selectSpec = selectSpec;
    }

    @Override
    public JoinClause<DTO> join(final String table) {
        return null;
    }

    @Override
    public WhereConditionClause<DTO> where(final String column) {
        return null;
    }
}
