package org.litebridge.orm.api.delete;

import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.delete.impl.AbstractDeletor;
import org.litebridge.orm.api.dto.delete.DtoDeleteWhereConditionClauseTerminalImpl;
import org.litebridge.orm.api.sql.delete.SqlDeleteWhereConditionClauseTerminalImpl;

public sealed interface DeleteTerminal extends DeleteQuery permits AbstractDeletor, DtoDeleteWhereConditionClauseTerminalImpl, SqlDeleteWhereConditionClauseTerminalImpl {

    UpdateResult execute();

}
