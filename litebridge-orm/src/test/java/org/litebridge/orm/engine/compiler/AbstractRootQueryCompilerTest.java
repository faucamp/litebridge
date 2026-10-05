package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.QueryNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractRootQueryCompilerTest {

    @Test
    void constructorStoresLitebridgeContext() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);

        // When
        final AbstractRootQueryCompiler compiler = new QueryCompiler(context);

        // Then
        assertNotNull(compiler);
        assertSame(context, compiler.litebridgeContext);
    }

    @Test
    void flattenNullReturnsEmptyList() {
        // When
        final List<QueryNode> nodes = AbstractRootQueryCompiler.flatten(null);

        // Then
        assertTrue(nodes.isEmpty());
    }

    @Test
    void flattenSingleNodeReturnsSingleElementList() {
        // Given
        final DeleteNode node = new DeleteNode(null, "users", null);

        // When
        final List<QueryNode> nodes = AbstractRootQueryCompiler.flatten(node);

        // Then
        assertEquals(1, nodes.size());
        assertSame(node, nodes.getFirst());
    }

    @Test
    void flattenChainReversesToRootToLeafOrder() {
        // Given
        final DeleteNode node1 = new DeleteNode(null, "users", null);
        final DeleteNode node2 = new DeleteNode(node1, "users", null);
        final DeleteNode node3 = new DeleteNode(node2, "users", null);

        // When
        final List<QueryNode> nodes = AbstractRootQueryCompiler.flatten(node3);

        // Then
        assertEquals(3, nodes.size());
        assertSame(node1, nodes.get(0));
        assertSame(node2, nodes.get(1));
        assertSame(node3, nodes.get(2));
    }
}
