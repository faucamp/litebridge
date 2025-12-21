package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.model.SelectField;
import org.litebridge.orm.api.select.FromClause;
import org.litebridge.orm.api.select.SelectClause;

import java.util.Arrays;

public abstract class AbstractSelectClause<DTO> implements SelectClause<DTO> {

    public FromClause<DTO> select(final String... columns) {
        final SelectField[] selectFields = Arrays.stream(columns)
                .map(SelectField::new)
                .toArray(SelectField[]::new);

        return select(selectFields);
    }

}
