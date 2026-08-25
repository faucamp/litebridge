//package org.litebridge.orm.api.update;
//
//import org.litebridge.db.spi.update.UpdateResult;
//import org.litebridge.orm.api.update.DtoUpdateWhereConditionClauseTerminalImpl;
//import org.litebridge.orm.api.update.SqlUpdateWhereConditionClauseTerminalImpl;
//import org.litebridge.orm.api.update.model.UpdateSpec;
//
//public sealed interface UpdateTerminal extends UpdateQuery permits DtoUpdateWhereConditionClauseTerminalImpl, SqlUpdateWhereConditionClauseTerminalImpl {
//
//    /**
//     * Executes the update.
//     * @return the update result.
//     */
//    UpdateResult execute();
//
//}
