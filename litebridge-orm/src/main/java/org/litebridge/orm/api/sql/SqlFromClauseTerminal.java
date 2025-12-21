package org.litebridge.orm.api.sql;

import org.litebridge.orm.api.select.FromClauseTerminal;
import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.impl.DelegatingSelectTerminal;
import org.litebridge.orm.api.select.impl.SelectSpec;

import java.util.Map;

public class SqlFromClauseTerminal extends DelegatingSelectTerminal<Map<String, Object>> implements FromClauseTerminal<Map<String, Object>> {

    private final SelectSpec selectSpec;

    public SqlFromClauseTerminal(final SelectSpec selectSpec, final SelectTerminal<Map<String, Object>> selectTerminal) {
        super(selectTerminal);
        this.selectSpec = selectSpec;
    }

    @Override
    public JoinClause<Map<String, Object>> join(final String table) {
        return null;
    }

    @Override
    public WhereConditionClause<Map<String, Object>> where(final String column) {
        return null;
    }
}
