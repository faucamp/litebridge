package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InsertQueryCompilerTest {

    @Test
    void createCompilationContextThrowsWhenNotInsertNode() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final InsertQueryCompiler compiler = new InsertQueryCompiler(context);
        final DeleteNode deleteNode = new DeleteNode(null, "items", null);

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> compiler.createCompilationContext(deleteNode));
        assertEquals("Expected InsertNode, but got " + deleteNode, ex.getMessage());
    }

    @Test
    void createCompilationContextSucceedsForInsertNode() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache metadataCache = mock(TableMetaDataCache.class);
        final Table table = new Table("items");
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of());

        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.tableMetaDataCache()).thenReturn(metadataCache);
        when(tableRegistry.getOrCreateSpiTable("items")).thenReturn(table);
        when(metadataCache.ensureTableMetaData(table)).thenReturn(metaData);

        final InsertQueryCompiler compiler = new InsertQueryCompiler(context);
        final InsertNode insertNode = new InsertNode("items", null, new String[0]);

        // When
        final InsertCompilationContext compilationContext = compiler.createCompilationContext(insertNode);

        // Then
        assertNotNull(compilationContext);
    }

    @Test
    void applyNodeInsertNodeIsIgnored() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final InsertQueryCompiler compiler = new InsertQueryCompiler(context);
        final InsertCompilationContext compilationContext = mock(InsertCompilationContext.class);
        final InsertNode insertNode = new InsertNode("items", null, new String[0]);

        // When
        compiler.applyNode(insertNode, compilationContext);

        // Then: no exception, nothing called on compilationContext
    }

    @Test
    void applyNodeInsertValuesNodeAddsRowBindValues() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final InsertQueryCompiler compiler = new InsertQueryCompiler(context);
        final InsertCompilationContext compilationContext = mock(InsertCompilationContext.class);
        final InsertNode insertNode = new InsertNode("items", null, new String[]{"name"});
        final InsertValuesNode valuesNode = new InsertValuesNode(insertNode, new Object[]{"Alice"});

        // When
        compiler.applyNode(valuesNode, compilationContext);

        // Then
        verify(compilationContext).addRowBindValues(List.of("Alice"));
    }

    @Test
    void applyNodeUnsupportedNodeThrowsException() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final InsertQueryCompiler compiler = new InsertQueryCompiler(context);
        final InsertCompilationContext compilationContext = mock(InsertCompilationContext.class);
        final DeleteNode unsupportedNode = new DeleteNode(null, "items", null);

        // When & Then
        final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> compiler.applyNode(unsupportedNode, compilationContext));
        assertInstanceOf(UnsupportedOperationException.class, ex);
    }
}
