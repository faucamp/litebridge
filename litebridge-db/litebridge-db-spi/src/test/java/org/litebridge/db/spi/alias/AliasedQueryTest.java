package org.litebridge.db.spi.alias;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AliasedQueryTest {

    @Test
    void testAliasedQuery() {
        // Given
        final Select select = mock(Select.class);
        final AliasedQuery aliasedQuery = new AliasedQuery("myAlias", select);

        // Then
        assertEquals("myAlias", aliasedQuery.alias());
        assertEquals(select, aliasedQuery.target());
    }
}