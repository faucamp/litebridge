//package org.litebridge.db.spi.impl.function;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.Operation;
//import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;
//import org.litebridge.db.spi.query.Select;
//import org.litebridge.db.spi.sql.PreparedSql;
//import org.litebridge.db.spi.tx.ConnectionProvider;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class SubselectTest {
//
//    @Test
//    void toSql() {
//        // Given
//        final Select select = mock(Select.class);
//        final SelectSqlGenerator selectSqlGenerator = mock(SelectSqlGenerator.class);
//        final Operation operation = mock(Select.class);
//        final ConnectionProvider connectionProvider = mock(ConnectionProvider.class);
//        final Subselect subselect = new Subselect(select, selectSqlGenerator);
//        when(selectSqlGenerator.prepareSql(select, connectionProvider)).thenReturn(new PreparedSql("SELECT * FROM TEST"));
//
//        // When
//        final PreparedSql sql = subselect.toSql(operation, connectionProvider);
//
//        // Then
//        assertNotNull(sql);
//        assertEquals("SELECT * FROM TEST", sql.sql());
//    }
//}