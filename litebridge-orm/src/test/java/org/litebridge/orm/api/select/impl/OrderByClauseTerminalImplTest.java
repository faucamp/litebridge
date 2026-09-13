package org.litebridge.orm.api.select.impl;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class OrderByClauseTerminalImplTest {

    @Test
    void limit() {
        // Given
        final int limit = 10;
        final QueryNode node = mock(SelectNode.class);
        final SelectEngineTerminal selectEngineTerminal = mock(SelectEngineTerminal.class);
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);

        final OrderByClauseTerminalImpl<String> terminal = new OrderByClauseTerminalImpl<>(node, selectEngineTerminal, litebridgeContext);

        // When
        final SelectTerminal<String> result = terminal.limit(10);

        // Then
        assertNotNull(result);
    }
}
