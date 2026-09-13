package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.generator.ColumnValueGenerator;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.intent.ExpressionSpecArray;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;

import java.sql.Types;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InsertCompilationContextTest {

    private LitebridgeContext createMockContext() {
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache metadataCache = mock(TableMetaDataCache.class);
        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.tableMetaDataCache()).thenReturn(metadataCache);
        return context;
    }

    @Test
    void constructWithTableNameAndSpecifiedColumns() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData nameCol = new ColumnMetaData(table, "name", true, Types.VARCHAR, 100);
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of(nameCol));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final InsertNode insertNode = new InsertNode("items", null, new String[]{"name"});

        // When
        final InsertCompilationContext compilationContext = new InsertCompilationContext(insertNode, context);

        // Then
        assertTrue(compilationContext.getBindValues().isEmpty());
    }

    @Test
    void constructWithTableNameOmittedNotNullWithoutGeneratorThrowsException() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData nameCol = new ColumnMetaData(table, "name", true, Types.VARCHAR, 100);
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", false, Types.INTEGER, 0); // NOT NULL, no auto-inc, no generator
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol, nameCol));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final InsertNode insertNode = new InsertNode("items", null, new String[]{"name"});

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new InsertCompilationContext(insertNode, context));
        assertTrue(ex.getMessage().contains("NOT NULL column id omitted"));
    }

    @Test
    void constructWithDtoClassAndContextDtoClassFound() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData col = new ColumnMetaData(table, "user_name", true, Types.VARCHAR, 100);
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of(col));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.columnMetaDataForField("name")).thenReturn(col);
        when(ormTable.isManyToManyJoinTable()).thenReturn(false);

        when(context.tableRegistry().getTableInContext(UserDto.class, ContextDto.class)).thenReturn(ormTable);

        final InsertNode insertNode = new InsertNode(null, UserDto.class, ContextDto.class, new String[]{"name"}, null);

        // When
        final InsertCompilationContext compilationContext = new InsertCompilationContext(insertNode, context);

        // Then
        assertTrue(compilationContext.getBindValues().isEmpty());
    }

    @Test
    void constructWithDtoClassFallbackWhenTableInContextNull() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData col = new ColumnMetaData(table, "name", true, Types.VARCHAR, 100);
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of(col));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.columnMetaDataForField("name")).thenReturn(col);

        when(context.tableRegistry().getTableInContext(UserDto.class, ContextDto.class)).thenReturn(null);
        when(context.tableRegistry().getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final InsertNode insertNode = new InsertNode(null, UserDto.class, ContextDto.class, new String[]{"name"}, null);

        // When
        final InsertCompilationContext compilationContext = new InsertCompilationContext(insertNode, context);

        // Then
        assertTrue(compilationContext.getBindValues().isEmpty());
    }

    @Test
    void constructWithDtoClassManyToManyJoinTable() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("user_roles");
        final ColumnMetaData col = new ColumnMetaData(table, "role_id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of(col));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.isManyToManyJoinTable()).thenReturn(true);
        when(context.tableRegistry().getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final InsertNode insertNode = new InsertNode(null, UserDto.class, new String[]{"role_id"});

        // When
        final InsertCompilationContext compilationContext = new InsertCompilationContext(insertNode, context);

        // Then
        assertTrue(compilationContext.getBindValues().isEmpty());
    }

    @Test
    void constructWithEmptyColumnsInsertsAllColumns() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData col1 = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final ColumnMetaData col2 = new ColumnMetaData(table, "name", true, Types.VARCHAR, 100);
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of(col1, col2));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final InsertNode insertNode = new InsertNode("items", null, new String[0]);

        // When
        final InsertCompilationContext compilationContext = new InsertCompilationContext(insertNode, context);

        // Then
        final Insert insert = compilationContext.toOperation();
        assertEquals(2, insert.columns().size());
        assertFalse(insert.returnGeneratedKeys());
    }

    @Test
    void constructWithAutoIncrementOmittedColumnSetsReturnGeneratedColumns() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData nameCol = new ColumnMetaData(table, "name", true, Types.VARCHAR, 100);
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", false, Types.INTEGER, 0, 0, true, null, null); // auto-increment
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol, nameCol));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final InsertNode insertNode = new InsertNode("items", null, new String[]{"name"});

        // When
        final InsertCompilationContext compilationContext = new InsertCompilationContext(insertNode, context);
        final Insert insert = compilationContext.toOperation();

        // Then
        assertTrue(insert.returnGeneratedKeys());
    }

    @Test
    void constructWithGeneratorOmittedColumnAddsImplicitColumn() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData nameCol = new ColumnMetaData(table, "name", true, Types.VARCHAR, 100);
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", false, Types.INTEGER, 0, 0, false, null, generator);
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of(idCol, nameCol));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final InsertNode insertNode = new InsertNode("items", null, new String[]{"name"});

        // When
        final InsertCompilationContext compilationContext = new InsertCompilationContext(insertNode, context);
        final Insert insert = compilationContext.toOperation();

        // Then
        assertTrue(insert.returnGeneratedKeys());
        assertEquals(2, insert.columns().size());
        assertEquals("name", insert.columns().get(0).name());
        assertEquals("id", insert.columns().get(1).name());
    }

    @Test
    void constructWithExpressionSpecsColumnExpressionSpecAndQueryField() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final ColumnMetaData nameCol = new ColumnMetaData(table, "name", true, Types.VARCHAR, 100);
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of(idCol, nameCol));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.columnMetaDataForField("name")).thenReturn(nameCol);
        when(context.tableRegistry().getOrmTableOrThrow(UserDto.class)).thenReturn(ormTable);

        final ColumnExpressionSpec colSpec = new SelectColumnSpec(new Column(table, "id"));
        final QueryField queryField = new QueryField(UserDto.class, "name");
        final InsertNode insertNode = new InsertNode(null, UserDto.class, new ExpressionSpec[]{colSpec, queryField});

        // When
        final InsertCompilationContext compilationContext = new InsertCompilationContext(insertNode, context);
        final Insert insert = compilationContext.toOperation();

        // Then
        assertEquals(2, insert.columns().size());
    }

    @Test
    void constructWithExpressionSpecsUnsupportedTypeThrowsException() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of());
        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final ExpressionSpec unsupportedSpec = new ExpressionSpecArray(new ExpressionSpec[0]);
        final InsertNode insertNode = new InsertNode("items", null, new ExpressionSpec[]{unsupportedSpec});

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new InsertCompilationContext(insertNode, context));
        assertTrue(ex.getMessage().contains("Unsupported expression spec type"));
    }

    @Test
    void constructWithExpressionSpecsOmittedNotNullWithoutGeneratorThrowsException() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData nameCol = new ColumnMetaData(table, "name", true, Types.VARCHAR, 100);
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", false, Types.INTEGER, 0); // NOT NULL, no generator
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol, nameCol));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final ColumnExpressionSpec colSpec = new SelectColumnSpec(new Column(table, "name"));
        final InsertNode insertNode = new InsertNode("items", null, new ExpressionSpec[]{colSpec});

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new InsertCompilationContext(insertNode, context));
        assertTrue(ex.getMessage().contains("NOT NULL column id omitted"));
    }

    @Test
    void constructWithExpressionSpecsOmittedNotNullWithGenerator() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData nameCol = new ColumnMetaData(table, "name", true, Types.VARCHAR, 100);
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", false, Types.INTEGER, 0, 0, false, null, generator);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol, nameCol));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final ColumnExpressionSpec colSpec = new SelectColumnSpec(new Column(table, "name"));
        final InsertNode insertNode = new InsertNode("items", null, new ExpressionSpec[]{colSpec});

        // When
        final InsertCompilationContext compilationContext = new InsertCompilationContext(insertNode, context);
        final Insert insert = compilationContext.toOperation();

        // Then
        assertTrue(insert.returnGeneratedKeys());
        assertEquals(2, insert.columns().size());
    }

    @Test
    void addRowBindValuesValidationAndSuccess() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData nameCol = new ColumnMetaData(table, "name", true, Types.VARCHAR, 100);
        final ColumnMetaData ageCol = new ColumnMetaData(table, "age", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of(nameCol, ageCol));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final InsertNode insertNode = new InsertNode("items", null, new String[]{"name", "age"});
        final InsertCompilationContext compilationContext = new InsertCompilationContext(insertNode, context);

        // When size mismatch -> throws
        assertThrows(IllegalArgumentException.class,
                () -> compilationContext.addRowBindValues(List.of("Alice")));

        // When valid row added
        compilationContext.addRowBindValues(List.of("Alice", 30));

        // Then
        assertEquals(2, compilationContext.getBindValues().size());
        assertEquals(new BindValue("Alice", Types.VARCHAR), compilationContext.getBindValues().get(0));
        assertEquals(new BindValue(30, Types.INTEGER), compilationContext.getBindValues().get(1));

        final Insert insert = compilationContext.toOperation();
        assertEquals(1, insert.rows());
    }

    @Test
    void addRowBindValuesNullForNonNullableWithoutGeneratorThrowsException() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", false, Types.INTEGER, 0); // NOT NULL, no generator
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final InsertNode insertNode = new InsertNode("items", null, new String[]{"id"});
        final InsertCompilationContext compilationContext = new InsertCompilationContext(insertNode, context);

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> compilationContext.addRowBindValues(Collections.singletonList(null)));
        assertTrue(ex.getMessage().contains("NULL value not allowed for non-nullable column: id"));
    }

    @Test
    void addRowBindValuesNullForNonNullableWithGeneratorRemovesFromExplicitInsert() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", false, Types.INTEGER, 0, 0, false, null, generator);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final InsertNode insertNode = new InsertNode("items", null, new String[]{"id"});
        final InsertCompilationContext compilationContext = new InsertCompilationContext(insertNode, context);

        // When passing null for id -> value will be generated instead
        compilationContext.addRowBindValues(Collections.singletonList(null));

        // Then
        assertTrue(compilationContext.getBindValues().isEmpty());
        final Insert insert = compilationContext.toOperation();
        assertEquals(1, insert.columns().size());
        assertEquals("id", insert.columns().getFirst().name());
        assertEquals(generator, insert.columns().getFirst().generator());
    }

    static class UserDto {
        private String name;
    }

    static class ContextDto {}
}
