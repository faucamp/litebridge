package org.litebridge.orm.api.update;

import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.dto.update.DtoUpdateWhereConditionClauseTerminalImpl;
import org.litebridge.orm.api.sql.update.SqlUpdateWhereConditionClauseTerminalImpl;
import org.litebridge.orm.api.update.impl.AbstractUpdater;
import org.litebridge.orm.api.update.model.UpdateSpec;

public sealed interface UpdateTerminal extends UpdateQuery permits DtoUpdateWhereConditionClauseTerminalImpl, SqlUpdateWhereConditionClauseTerminalImpl, AbstractUpdater {

    /**
     * Returns the update specification.
     *
     * @return the update specification.
     */
    UpdateSpec updateSpec();

    /**
     * Executes the update.
     * @return the update result.
     */
    UpdateResult execute();

}
