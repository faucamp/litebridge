package org.litebridge.orm.api.register;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class PlaceHolderDatabaseProviderTest {

    @Test
    void testUnsupportedMethods() {
        PlaceHolderDatabaseProvider provider = new PlaceHolderDatabaseProvider();
        ConnectionProvider connectionProvider = mock(ConnectionProvider.class);
        PreparedSql preparedSql = mock(PreparedSql.class);
        Table table = new Table("TEST");

        assertThrows(UnsupportedOperationException.class, () -> provider.tableMetaData(table, connectionProvider));
        assertThrows(UnsupportedOperationException.class, () -> provider.insert(preparedSql, connectionProvider));
        assertThrows(UnsupportedOperationException.class, () -> provider.update(preparedSql, connectionProvider));
        assertThrows(UnsupportedOperationException.class, () -> provider.select(preparedSql, connectionProvider));
        assertThrows(UnsupportedOperationException.class, () -> provider.toSql(mock(org.litebridge.db.spi.query.Select.class), connectionProvider));
        assertThrows(UnsupportedOperationException.class, () -> provider.delete(preparedSql, connectionProvider));
        assertThrows(UnsupportedOperationException.class, () -> provider.nativeSqlQuery("SELECT 1", Collections.emptyList(), connectionProvider));
        assertThrows(UnsupportedOperationException.class, () -> provider.nativeSqlUpdate("UPDATE TEST SET COL = 1", Collections.emptyList(), connectionProvider));
        assertThrows(UnsupportedOperationException.class, () -> provider.getTypeConverter());
        assertThrows(UnsupportedOperationException.class, () -> provider.getSqlFunctionRegistry());
        assertThrows(UnsupportedOperationException.class, () -> provider.getAliasTransformer());

        assertNotNull(provider.getSequenceColumnValueGenerator("SEQ"));
    }
}
