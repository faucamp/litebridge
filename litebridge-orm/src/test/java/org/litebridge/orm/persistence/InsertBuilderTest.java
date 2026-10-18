package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.compiler.QueryCompiler;
import org.litebridge.orm.persistence.alias.DefaultAliasGenerator;

import java.util.Collections;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InsertBuilderTest {

    @Test
    void build() {
        // Given
        final OrmTable table = mock(OrmTable.class);
        final TableMetaData metaData = mock(TableMetaData.class);
        when(table.metaData()).thenReturn(metaData);
        when(metaData.table()).thenReturn(new Table("TEST_TABLE"));
        when(metaData.primaryKey()).thenReturn(Collections.emptyList());
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        when(litebridgeContext.aliasGenerator()).thenReturn(new DefaultAliasGenerator());
        final QueryCompiler queryCompiler = mock(QueryCompiler.class);
        when(litebridgeContext.createQueryCompiler()).thenReturn(queryCompiler);
        when(queryCompiler.compile(any(QueryNode.class))).thenReturn(mock(PreparedOperation.class));

        final InsertBuilder builder = new InsertBuilder(new Object(), table, null, litebridgeContext);
        final LinkedHashMap<String, Object> row = new LinkedHashMap<>();
        row.put("TEST_COLUMN", "TEST_VALUE");
        builder.addRow(row);

        // When
        final PreparedOperation result = builder.build();

        // Then
        assertNotNull(result);
        verify(queryCompiler).compile(any(InsertValuesNode.class));
    }
}
