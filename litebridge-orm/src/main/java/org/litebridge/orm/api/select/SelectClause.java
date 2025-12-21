package org.litebridge.orm.api.select;

import org.litebridge.orm.api.model.SelectField;

public interface SelectClause<DTO> {

    FromClause<DTO> select(final SelectField... selectFields);
}
