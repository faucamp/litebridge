package org.litebridge.orm.api.sql;

import org.litebridge.orm.api.model.SelectField;
import org.litebridge.orm.api.select.FromClause;
import org.litebridge.orm.api.select.impl.AbstractSelectClause;

import java.util.Map;

public class SqlSelectClause extends AbstractSelectClause<Map<String, Object>> {

    @Override
    public FromClause<Map<String, Object>> select(final SelectField... selectFields) {
        return null;
    }
}
