package org.litebridge.orm.api.register;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.UpdateResult;

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
        assertThrows(UnsupportedOperationException.class, () -> provider.executeUpdate(preparedSql, UpdateResult.class, connectionProvider));
        assertThrows(UnsupportedOperationException.class, () -> provider.executeQuery(preparedSql, connectionProvider));
        assertThrows(UnsupportedOperationException.class, () -> provider.toSql(mock(org.litebridge.db.spi.query.Select.class), connectionProvider));
        assertThrows(UnsupportedOperationException.class, provider::typeConverter);
        assertThrows(UnsupportedOperationException.class, provider::sqlFunctionRegistry);
        assertThrows(UnsupportedOperationException.class, provider::aliasTransformer);

        assertNotNull(provider.sequenceColumnValueGenerator("SEQ"));
    }
}
