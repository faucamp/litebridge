package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.compiler.QueryCompiler;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InsertBuilderTest {

    @Test
    void build() {
        // Given
        final OrmTable table = mock(OrmTable.class);
        final TableMetaData metaData = mock(TableMetaData.class);
        when(table.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(new Table("TEST_TABLE"));
        when(metaData.primaryKey()).thenReturn(Collections.emptyList());
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final QueryCompiler queryCompiler = mock(QueryCompiler.class);
        when(litebridgeContext.createQueryCompiler()).thenReturn(queryCompiler);

        final InsertBuilder builder = new InsertBuilder(table, litebridgeContext);

        // When
        final PreparedOperation result = builder.build();

        // Then
        assertNotNull(result);
        assertInstanceOf(Insert.class, result.operation());
        final Insert insert = (Insert) result.operation();
        assertEquals("TEST_TABLE", insert.table().name());
        assertEquals(1, insert.rows().size());
        assertFalse(insert.returnGeneratedKeys());
    }
}
