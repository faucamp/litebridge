package org.litebridge.orm.api.select;

import java.util.Map;

public class SelectClause<DTO> {

    public FromClause<Map<String, Object>> select(final String... columns) {
        throw new UnsupportedOperationException();
    }

    public FromClauseTerminal<DTO> select(final Class<DTO> dtoClass) {
        throw new UnsupportedOperationException();
    }
}
