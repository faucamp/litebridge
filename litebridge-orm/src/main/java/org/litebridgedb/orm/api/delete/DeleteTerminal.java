package org.litebridgedb.orm.api.delete;

import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.orm.api.delete.impl.AbstractDeletor;
import org.litebridgedb.orm.api.dto.delete.DtoDeleteWhereConditionClauseTerminalImpl;
import org.litebridgedb.orm.api.sql.delete.SqlDeleteWhereConditionClauseTerminalImpl;

public sealed interface DeleteTerminal extends DeleteQuery permits AbstractDeletor, DtoDeleteWhereConditionClauseTerminalImpl, SqlDeleteWhereConditionClauseTerminalImpl {

    UpdateResult execute();

}
