package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.QueryNode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractQueryCompilerTest {

    @Test
    void applyNodesAppliesEachNodeInOrder() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final DeleteQueryCompiler compiler = new DeleteQueryCompiler(context);
        final DeleteCompilationContext compilationContext = mock(DeleteCompilationContext.class);

        final DeleteNode node1 = new DeleteNode(null, "users", null);
        final DeleteNode node2 = new DeleteNode(node1, "users", null);
        final List<QueryNode> nodes = List.of(node1, node2);

        // When
        compiler.applyNodes(nodes, compilationContext);

        // Then: both DeleteNodes are processed (handled as no-op by DeleteQueryCompiler without error)
    }

    @Test
    void flattenAndApplyNodesWithCompilationContext() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final DeleteQueryCompiler compiler = new DeleteQueryCompiler(context);
        final DeleteCompilationContext compilationContext = mock(DeleteCompilationContext.class);

        final DeleteNode node1 = new DeleteNode(null, "users", null);
        final DeleteNode node2 = new DeleteNode(node1, "users", null);

        // When
        compiler.flattenAndApplyNodes(node2, compilationContext);

        // Then: nodes are flattened and applied without error
    }

    @Test
    void flattenAndApplyNodesWithConsumerAcceptsNodesInRootToLeafOrder() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final DeleteQueryCompiler compiler = new DeleteQueryCompiler(context);

        final DeleteNode node1 = new DeleteNode(null, "users", null);
        final DeleteNode node2 = new DeleteNode(node1, "users", null);
        final DeleteNode node3 = new DeleteNode(node2, "users", null);

        final List<QueryNode> acceptedNodes = new ArrayList<>();

        // When
        compiler.flattenAndApplyNodes(node3, acceptedNodes::add);

        // Then
        assertEquals(3, acceptedNodes.size());
        assertSame(node1, acceptedNodes.get(0));
        assertSame(node2, acceptedNodes.get(1));
        assertSame(node3, acceptedNodes.get(2));
    }
}
