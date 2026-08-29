package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.compiler.QueryCompiler;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeleteBuilderTest {

    @Test
    void build() {
        // Given
        final OrmTable ormTable = mock(OrmTable.class);
        final TableMetaData tableMetaData = new TableMetaData("catalog", "schema", "table", Collections.emptyList(), Collections.emptyList());
        when(ormTable.getMetaData()).thenReturn(tableMetaData);
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final QueryCompiler queryCompiler = mock(QueryCompiler.class);
        when(litebridgeContext.createQueryCompiler()).thenReturn(queryCompiler);

        final DeleteBuilder builder = new DeleteBuilder(ormTable, litebridgeContext);
        final QueryNode queryNode = new DeleteNode(null, tableMetaData.qualifiedName(), null);
        builder.where(queryNode);

        // When
        final PreparedOperation result = builder.build();

        // Then
        assertNotNull(result);
        assertInstanceOf(Delete.class, result.operation());
        final Delete delete = (Delete) result.operation();
        assertEquals(tableMetaData.toTable(), delete.table());
    }
}
