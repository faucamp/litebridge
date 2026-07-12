package org.litebridge.orm.api.dto.delete;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoDeletorTest {

    @Test
    @SuppressWarnings("unchecked")
    void testWhere() {
        final OrmTable ormTable = mock(OrmTable.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final SelectExpressionMapper mapper = mock(SelectExpressionMapper.class);
        final LitebridgeContext context = mock(LitebridgeContext.class);

        final Table table = new Table("TEST");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        final ColumnMetaData col = new ColumnMetaData(table, "COL", true, Types.VARCHAR);
        when(ormTable.getColumnForFieldName("field")).thenReturn(col);

        final DtoDeletor<Object> deletor = new DtoDeletor<>(Object.class, ormTable, databaseProvider, mapper, context);
        
        final DtoDeleteWhereConditionClause<Object> clause = deletor.where("field");
        assertNotNull(clause);
    }
}
