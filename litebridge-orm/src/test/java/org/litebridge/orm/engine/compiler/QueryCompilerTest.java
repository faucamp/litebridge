package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.DefaultAliasTransformer;
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
import org.litebridge.orm.engine.ast.WhenMatchedNode;
import org.litebridge.orm.engine.ast.WhenNotMatchedNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.DefaultAliasGenerator;
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
    void compile_merge_orderingAndBindValueIndices() {
        // Given
        final Table target = new Table("items");
        final Table source = new Table("incoming");
        final ColumnMetaData targetId = new ColumnMetaData(target, "id", true, Types.INTEGER, 0);
        final ColumnMetaData targetBalance = new ColumnMetaData(target, "balance", true, Types.INTEGER, 0);
        final ColumnMetaData sourceId = new ColumnMetaData(source, "id", true, Types.INTEGER, 0);
        final TableMetaData targetMetadata = new TableMetaData(target, List.of(), List.of(targetId, targetBalance));
        final TableMetaData sourceMetadata = new TableMetaData(source, List.of(), List.of(sourceId));

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache metadataCache = mock(TableMetaDataCache.class);
        when(tableRegistry.getOrmTable("items")).thenReturn(null);
        when(tableRegistry.getOrCreateSpiTable("items")).thenReturn(target);
        when(tableRegistry.getOrCreateSpiTable("incoming")).thenReturn(source);
        when(metadataCache.ensureTableMetaData(target)).thenReturn(targetMetadata);
        when(metadataCache.ensureTableMetaData(source)).thenReturn(sourceMetadata);

        final LitebridgeContext context = context(tableRegistry, metadataCache);
        when(context.mode()).thenReturn(LitebridgeContext.Mode.SQL);
        final SelectExpressionMapper expressionMapper = mock(SelectExpressionMapper.class);
        when(expressionMapper.toSelectExpression(any(), eq(true))).thenReturn(mock(SelectExpression.class));
        final TypeConverter typeConverter = mock(TypeConverter.class);
        when(typeConverter.getSqlDataType(Integer.class)).thenReturn(Types.INTEGER);
        when(typeConverter.convert(any(), eq(Types.INTEGER))).thenAnswer(inv -> inv.getArgument(0));
        when(context.selectExpressionMapper()).thenReturn(expressionMapper);
        when(context.typeConverter()).thenReturn(typeConverter);

        final QueryCompiler compiler = new QueryCompiler(context);

        // MERGE INTO items USING incoming ON incoming.id = 1
        final MergeNode root = new MergeNode("items", null);
        final ConditionNode onCondition = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 1);
        final UsingNode using = new UsingNode(root, "incoming", null, onCondition);

        // WHEN MATCHED (UPDATE SET balance = 500 WHERE id < 5)
        final UpdateNode updateNode = new UpdateNode(null, "items", null);
        final SetNode setNode = new SetNode(updateNode, "balance", 500);
        final ConditionNode whereCondition = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.LT, 5);
        final WhereNode whereNode = new WhereNode(setNode, whereCondition);
        final WhenMatchedNode whenMatchedNode = new WhenMatchedNode(using, whereNode);

        // WHEN NOT MATCHED INSERT (id, balance) VALUES (123, 0)
        final InsertNode insertNode = new InsertNode("items", null, new String[]{"id", "balance"});
        final InsertValuesNode insertValuesNode = new InsertValuesNode(insertNode, new Object[]{123, 0});
        final WhenNotMatchedNode whenNotMatchedNode = new WhenNotMatchedNode(whenMatchedNode, null, insertValuesNode);

        // When
        final PreparedOperation result = compiler.compile(whenNotMatchedNode);

        // Then
        assertInstanceOf(Merge.class, result.operation());
        final Merge merge = (Merge) result.operation();

        // 1. ON condition bind value is first (index 0)
        assertEquals(5, result.bindValues().size());
        assertEquals(new BindValue(1, Types.INTEGER), result.bindValues().get(0));
        // 2. WHEN MATCHED condition bind value is second (index 1)
        assertEquals(new BindValue(5, Types.INTEGER), result.bindValues().get(1));
        // 3. WHEN MATCHED UPDATE SET bind value is third (index 2)
        assertEquals(new BindValue(500, Types.INTEGER), result.bindValues().get(2));
        // 4. WHEN NOT MATCHED INSERT bind values are fourth and fifth (index 3 and 4)
        assertEquals(new BindValue(123, Types.INTEGER), result.bindValues().get(3));
        assertEquals(new BindValue(0, Types.INTEGER), result.bindValues().get(4));

        // Check UpdateColumn bind indices
        final Merge.WhenMatched<Merge.WhenMatchedOperation> matchedClause = merge.whenMatched().getFirst();
        final Merge.MergeUpdate updateOp = (Merge.MergeUpdate) matchedClause.operation();
        assertEquals(1, updateOp.columns().size());
        assertEquals("balance", updateOp.columns().getFirst().name());
        assertEquals(2, updateOp.columns().getFirst().bindValueIndex());

        final Merge.WhenMatched<Merge.MergeInsert> notMatchedClause = merge.whenNotMatched().getFirst();
        final Merge.MergeInsert insertOp = notMatchedClause.operation();
        assertEquals(2, insertOp.columns().size());
        assertEquals("id", insertOp.columns().get(0).name());
        assertEquals(3, insertOp.columns().get(0).bindValueIndex());
        assertEquals("balance", insertOp.columns().get(1).name());
        assertEquals(4, insertOp.columns().get(1).bindValueIndex());
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
        when(context.aliasGenerator()).thenReturn(new DefaultAliasGenerator(new DefaultAliasTransformer()));
        return context;
    }
}
