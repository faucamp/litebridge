package org.litebridge.orm.api.sql.update;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.sql.delete.SqlDeletor;
import org.litebridge.orm.api.sql.delete.SqlDeleteFromClause;
import org.litebridge.orm.api.sql.delete.SqlDeleteWhereConditionClauseTerminalImpl;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class SqlUpdateDeleteExtraTest {

    @Test
    void sqlUpdateWhereConditionClauseTerminalImpl_and() {
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        Table table = new Table("cat", "sch", "tab");
        SqlUpdater updater = new SqlUpdater(table, databaseProvider);
        SqlUpdateWhereConditionClauseTerminalImpl terminal = new SqlUpdateWhereConditionClauseTerminalImpl(updater);
        
        assertNotNull(terminal.and("col2"));
    }

    @Test
    void sqlDeleteFromClause_methods() {
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        SqlDeleteFromClause fromClause = new SqlDeleteFromClause(databaseProvider);
        
        assertNotNull(fromClause.from("TABLE"));
    }

    @Test
    void sqlDeleteWhereConditionClauseTerminalImpl_and() {
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        Table table = new Table("cat", "sch", "tab");
        SqlDeletor deletor = new SqlDeletor(table, databaseProvider);
        SqlDeleteWhereConditionClauseTerminalImpl terminal = new SqlDeleteWhereConditionClauseTerminalImpl(deletor);
        
        assertNotNull(terminal.and("col2"));
    }
}
