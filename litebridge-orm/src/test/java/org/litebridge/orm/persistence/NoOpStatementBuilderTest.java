package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoOpStatementBuilderTest {

    private NoOpStatementBuilder noOpStatementBuilder = new NoOpStatementBuilder();

    @Test
    void statementChain() {
        // When/Then
        assertThrows(UnsupportedOperationException.class, () -> noOpStatementBuilder.statementChain());
    }

    @Test
    void build() {
        // When/Then
        assertThrows(UnsupportedOperationException.class, () -> noOpStatementBuilder.build());
    }
}