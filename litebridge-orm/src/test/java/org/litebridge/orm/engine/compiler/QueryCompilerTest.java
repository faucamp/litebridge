package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.Update;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.engine.ast.UpdateNode;
import org.litebridge.orm.engine.ast.UsingNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;

import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QueryCompilerTest {

    @Test
    void compile_delete_reuseLazyCompiler() {
        // Given
        final Table table = new Table("items");
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getOrCreateSpiTable("items")).thenReturn(table);
        final LitebridgeContext context = context(tableRegistry, mock(TableMetaDataCache.class));
        final QueryCompiler compiler = new QueryCompiler(context);
        final DeleteNode root = new DeleteNode(null, "items", null);

        // When
        final PreparedOperation first = compiler.compile(root);
        final PreparedOperation second = compiler.compile(root);

        // Then
        assertInstanceOf(Delete.class, first.operation());
        assertInstanceOf(Delete.class, second.operation());
        assertTrue(first.bindValues().isEmpty());
    }

    @Test
    void compile_insert_rowsAndPreservesBindValues() {
        // Given
        final Table table = new Table("items");
        final ColumnMetaData column = new ColumnMetaData(table, "name", true, Types.VARCHAR, 255);
        final TableMetaData metadata = new TableMetaData(table, List.of(), List.of(column));
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache metadataCache = mock(TableMetaDataCache.class);
        when(tableRegistry.getOrCreateSpiTable("items")).thenReturn(table);
        when(metadataCache.ensureTableMetaData(table)).thenReturn(metadata);
        final QueryCompiler compiler = new QueryCompiler(context(tableRegistry, metadataCache));
        final InsertNode root = new InsertNode("items", null, new String[]{"name"});
        final InsertValuesNode values = new InsertValuesNode(root, new Object[]{"Ada"});

        // When
        final PreparedOperation result = compiler.compile(values);

        // Then
        assertInstanceOf(Insert.class, result.operation());
        assertEquals(List.of(new BindValue("Ada", Types.VARCHAR)), result.bindValues());
    }

    @Test
    void compile_update_setValues() {
        // Given
        final Table table = new Table("items");
        final ColumnMetaData column = new ColumnMetaData(table, "name", true, Types.VARCHAR, 255);
        final TableMetaData metadata = new TableMetaData(table, List.of(), List.of(column));
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache metadataCache = mock(TableMetaDataCache.class);
        when(tableRegistry.getOrCreateSpiTable("items")).thenReturn(table);
        when(metadataCache.ensureTableMetaData(table)).thenReturn(metadata);
        final QueryCompiler compiler = new QueryCompiler(context(tableRegistry, metadataCache));
        final UpdateNode root = new UpdateNode(null, "items", null);
        final SetNode set = new SetNode(root, "name", "Ada");

        // When
        final PreparedOperation result = compiler.compile(set);

        // Then
        assertInstanceOf(Update.class, result.operation());
        assertEquals(List.of(new BindValue("Ada", Types.VARCHAR)), result.bindValues());
    }

    @Test
    void compile_merge_usingClause() {
        // Given
        final Table target = new Table("items");
        final Table source = new Table("incoming");
        final ColumnMetaData column = new ColumnMetaData(target, "id", true, Types.INTEGER, 0);
        final TableMetaData metadata = new TableMetaData(target, List.of(), List.of(column));
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache metadataCache = mock(TableMetaDataCache.class);
        when(tableRegistry.getOrmTable("items")).thenReturn(null);
        when(tableRegistry.getOrCreateSpiTable("items")).thenReturn(target);
        when(tableRegistry.getOrCreateSpiTable("incoming")).thenReturn(source);
        when(metadataCache.ensureTableMetaData(target)).thenReturn(metadata);
        final LitebridgeContext context = context(tableRegistry, metadataCache);
        when(context.mode()).thenReturn(LitebridgeContext.Mode.SQL);
        final SelectExpressionMapper expressionMapper = mock(SelectExpressionMapper.class);
        when(expressionMapper.toSelectExpression(any(), eq(true))).thenReturn(mock(SelectExpression.class));
        final TypeConverter typeConverter = mock(TypeConverter.class);
        when(typeConverter.getSqlDataType(Integer.class)).thenReturn(Types.INTEGER);
        when(context.selectExpressionMapper()).thenReturn(expressionMapper);
        when(context.typeConverter()).thenReturn(typeConverter);
        final QueryCompiler compiler = new QueryCompiler(context);
        final MergeNode root = new MergeNode("items", null);
        final ConditionNode condition = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 1);
        final UsingNode using = new UsingNode(root, "incoming", null, condition);

        // When
        final PreparedOperation result = compiler.compile(using);

        // Then
        assertInstanceOf(Merge.class, result.operation());
    }

    @Test
    void compile_selectRoot() {
        // Given
        final Table table = new Table("items");
        final TableMetaData metadata = new TableMetaData(table, List.of(), List.of());
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache metadataCache = mock(TableMetaDataCache.class);
        when(tableRegistry.getOrCreateSpiTable("items")).thenReturn(table);
        when(metadataCache.ensureTableMetaData(table)).thenReturn(metadata);
        final LitebridgeContext context = context(tableRegistry, metadataCache);
        when(context.aliasGenerator()).thenReturn(new NoOpAliasGenerator());
        when(context.selectExpressionMapper()).thenReturn(mock(SelectExpressionMapper.class));
        final QueryCompiler compiler = new QueryCompiler(context);
        final SelectNode root = new SelectNode("items", null, null, null, new ExpressionSpec[0], null);

        // When
        final PreparedOperation result = compiler.compile(root);

        // Then
        assertInstanceOf(Select.class, result.operation());
    }

    @Test
    void compile_unsupportedRootQueryNode() {
        // Given
        final QueryCompiler compiler = new QueryCompiler(mock(LitebridgeContext.class));
        final WhereNode unsupportedRoot = new WhereNode(null,
                new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, "value"));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> compiler.compile(unsupportedRoot));
    }

    private static LitebridgeContext context(final TableRegistry tableRegistry,
                                             final TableMetaDataCache metadataCache) {
        final LitebridgeContext context = mock(LitebridgeContext.class);
        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.tableMetaDataCache()).thenReturn(metadataCache);
        return context;
    }
}
