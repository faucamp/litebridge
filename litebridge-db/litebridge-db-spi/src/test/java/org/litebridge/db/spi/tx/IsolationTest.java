package org.litebridge.db.spi.tx;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IsolationTest {

    @Test
    void testLevel() {
        assertEquals(-1, Isolation.DEFAULT.level());
        assertEquals(Connection.TRANSACTION_READ_UNCOMMITTED, Isolation.READ_UNCOMMITTED.level());
        assertEquals(Connection.TRANSACTION_READ_COMMITTED, Isolation.READ_COMMITTED.level());
        assertEquals(Connection.TRANSACTION_REPEATABLE_READ, Isolation.REPEATABLE_READ.level());
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, Isolation.SERIALIZABLE.level());
    }

    @Test
    void testValueOf() {
        assertEquals(Isolation.DEFAULT, Isolation.valueOf("DEFAULT"));
        assertEquals(Isolation.READ_UNCOMMITTED, Isolation.valueOf("READ_UNCOMMITTED"));
        assertEquals(Isolation.READ_COMMITTED, Isolation.valueOf("READ_COMMITTED"));
        assertEquals(Isolation.REPEATABLE_READ, Isolation.valueOf("REPEATABLE_READ"));
        assertEquals(Isolation.SERIALIZABLE, Isolation.valueOf("SERIALIZABLE"));
    }

    @Test
    void testValues() {
        assertEquals(5, Isolation.values().length);
    }
}
