package org.litebridge.orm.api.sql;

import org.litebridge.orm.api.select.impl.FromClauseTerminalImpl;
import org.litebridge.orm.api.select.impl.JoinClauseImpl;
import org.litebridge.orm.api.select.impl.JoinConditionClauseImpl;
import org.litebridge.orm.api.select.impl.JoinConditionClauseTerminalImpl;

import java.util.LinkedHashMap;

public class SqlFromClauseTerminal extends FromClauseTerminalImpl<LinkedHashMap<String, Object>,
        JoinClauseImpl<LinkedHashMap<String, Object>>,
        JoinConditionClauseImpl<LinkedHashMap<String, Object>, JoinConditionClauseTerminalImpl<LinkedHashMap<String, Object>>>,
        JoinConditionClauseTerminalImpl<LinkedHashMap<String, Object>>> {

    public SqlFromClauseTerminal(final SqlSelector delegate) {
        super(delegate);
    }
}
