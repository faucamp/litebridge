package org.litebridge.orm.api.delete;

import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.delete.impl.AbstractDeletor;
import org.litebridge.orm.api.dto.delete.DtoDeleteWhereConditionClauseTerminalImpl;
import org.litebridge.orm.api.sql.delete.SqlDeleteWhereConditionClauseTerminalImpl;

/**
 * Represents the final stage of a delete query that can be executed.
 */
public sealed interface DeleteTerminal extends DeleteQuery permits AbstractDeletor, DtoDeleteWhereConditionClauseTerminalImpl, SqlDeleteWhereConditionClauseTerminalImpl {

    /**
     * Executes the delete query and returns the result.
     *
     * @return the result of the delete operation
     */
    UpdateResult execute();

}
