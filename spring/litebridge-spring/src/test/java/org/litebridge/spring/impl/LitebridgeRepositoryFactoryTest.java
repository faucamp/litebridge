package org.litebridge.spring.impl;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.LitebridgeCore;
import org.litebridge.spring.repository.LitebridgeQueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.ValueExpressionDelegate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class LitebridgeRepositoryFactoryTest {

    @Test
    void getQueryLookupStrategy() {
        // Given
        final LitebridgeCore litebridge = mock(LitebridgeCore.class);
        final LitebridgeRepositoryFactory factory = new LitebridgeRepositoryFactory(litebridge);

        // When
        final Optional<QueryLookupStrategy> result = factory.getQueryLookupStrategy(QueryLookupStrategy.Key.USE_DECLARED_QUERY, mock(ValueExpressionDelegate.class));

        // Then
        assertTrue(result.isPresent());
        final QueryLookupStrategy strategy = result.get();
        assertInstanceOf(LitebridgeQueryLookupStrategy.class, strategy);
    }
}