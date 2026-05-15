package org.litebridgedb.orm.api.update;

import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.orm.api.dto.update.DtoUpdateWhereConditionClauseTerminalImpl;
import org.litebridgedb.orm.api.sql.update.SqlUpdateWhereConditionClauseTerminalImpl;
import org.litebridgedb.orm.api.update.impl.AbstractUpdater;
import org.litebridgedb.orm.api.update.model.UpdateSpec;

public sealed interface UpdateTerminal extends UpdateQuery permits DtoUpdateWhereConditionClauseTerminalImpl, SqlUpdateWhereConditionClauseTerminalImpl, AbstractUpdater {

    UpdateSpec updateSpec();

    UpdateResult execute();

}
