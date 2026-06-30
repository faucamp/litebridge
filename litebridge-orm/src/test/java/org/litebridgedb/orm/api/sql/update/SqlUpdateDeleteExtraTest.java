package org.litebridgedb.orm.api.sql.update;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.api.sql.delete.SqlDeleteWhereConditionClauseTerminalImpl;
import org.litebridgedb.orm.api.sql.delete.SqlDeletor;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class SqlUpdateDeleteExtraTest {

    @Test
    void sqlUpdateWhereConditionClauseTerminalImpl_and() {
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        Table table = new Table("cat", "sch", "tab");
        SqlUpdater updater = new SqlUpdater(table, databaseProvider, mock(SelectExpressionMapper.class), mock(LitebridgeContext.class));
        SqlUpdateWhereConditionClauseTerminalImpl terminal = new SqlUpdateWhereConditionClauseTerminalImpl(updater);

        assertNotNull(terminal.and("col2"));
    }

    @Test
    void sqlDeleteWhereConditionClauseTerminalImpl_and() {
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        Table table = new Table("cat", "sch", "tab");
        SqlDeletor deletor = new SqlDeletor(table, databaseProvider, mock(SelectExpressionMapper.class), mock(LitebridgeContext.class));
        SqlDeleteWhereConditionClauseTerminalImpl terminal = new SqlDeleteWhereConditionClauseTerminalImpl(deletor);

        assertNotNull(terminal.and("col2"));
    }
}
