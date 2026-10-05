package org.litebridge.orm.api.select.impl;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.LimitNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class LimitClauseTerminalImplTest {

    @Test
    void offset() {
        // Given
        final int offset = 20;
        final int limit = 10;
        final QueryNode node = mock(SelectNode.class);
        final SelectEngineTerminal selectEngineTerminal = mock(SelectEngineTerminal.class);
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);

        final LimitClauseTerminalImpl<String> terminal = new LimitClauseTerminalImpl<>(
                limit,
                node,
                selectEngineTerminal,
                litebridgeContext);

        // When
        final SelectTerminal<String> result = terminal.offset(offset);

        // Then
        assertNotNull(result);

        assertInstanceOf(LimitNode.class, terminal.node());
        final LimitNode limitNode = (LimitNode) terminal.node();
        assertEquals(offset, limitNode.offset());
        assertEquals(limit, limitNode.limit());
    }
}
