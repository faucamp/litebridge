package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.ForeignKeyConstraint;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.DefaultAliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.generator.ColumnValueGenerator;
import org.litebridge.db.spi.math.MathOperator;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.InsertDtoValuesNode;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.engine.ast.UsingNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;
import org.litebridge.orm.expression.intent.ConvertSpec;
import org.litebridge.orm.expression.intent.ExpressionSpecArray;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.DefaultAliasGenerator;
import org.litebridge.tracking.FieldAccessor;
import org.mockito.Mockito;

import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MergeCompilationContextTest {

    private LitebridgeContext createMockContext() {
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache metadataCache = mock(TableMetaDataCache.class);
        final SelectExpressionMapper expressionMapper = mock(SelectExpressionMapper.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class, Mockito.RETURNS_DEEP_STUBS);

        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.tableMetaDataCache()).thenReturn(metadataCache);
        when(context.selectExpressionMapper()).thenReturn(expressionMapper);
        when(context.typeConverter()).thenReturn(typeConverter);
        when(context.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        when(context.aliasGenerator()).thenReturn(new DefaultAliasGenerator(new DefaultAliasTransformer()));
        return context;
    }

    @Test
    void constructWithSqlModeTable() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        when(context.tableRegistry().getOrmTable("items")).thenReturn(null);
        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final MergeNode mergeNode = new MergeNode("items", null);

        // When
        final MergeCompilationContext compilationContext = new MergeCompilationContext(mergeNode, context);

        // Then
        assertNotNull(compilationContext.onConditionGroupStack());
        assertThrows(NullPointerException.class, compilationContext::conditionContext);
    }

    @Test
    void constructWithDtoClass() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(context.tableRegistry().getOrmTable(UserDto.class)).thenReturn(ormTable);

        final MergeNode mergeNode = new MergeNode(null, UserDto.class);

        // When
        final MergeCompilationContext compilationContext = new MergeCompilationContext(mergeNode, context);

        // Then
        assertNotNull(compilationContext);
    }

    @Test
    void constructWithTableNameThatIsDto() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(context.tableRegistry().getOrmTable("users")).thenReturn(ormTable);

        final MergeNode mergeNode = new MergeNode("users", null);

        // When
        final MergeCompilationContext compilationContext = new MergeCompilationContext(mergeNode, context);

        // Then
        assertNotNull(compilationContext);
    }

    @Test
    void setUsingNodeWithTableAndDtoClass() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        when(context.tableRegistry().getOrmTable("items")).thenReturn(null);
        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final Table usingTable = new Table("incoming");
        when(context.tableRegistry().getOrCreateSpiTable("incoming")).thenReturn(usingTable);

        final MergeCompilationContext compilationContext = new MergeCompilationContext(new MergeNode("items", null), context);

        // When using table name
        compilationContext.setUsingNode(new UsingNode(null, "incoming", null, null));

        // Then
        assertEquals(MergeCompilationContext.ConditionContext.ON, compilationContext.conditionContext());

        // When using DTO class
        final OrmTable usingOrmTable = mock(OrmTable.class);
        final TableMetaData usingMeta = new TableMetaData(new Table("users"), List.of("id"), List.of(idCol));
        when(usingOrmTable.getMetaData()).thenReturn(usingMeta);
        when(context.tableRegistry().getOrmTable(UserDto.class)).thenReturn(usingOrmTable);

        compilationContext.setUsingNode(new UsingNode(null, null, UserDto.class, null));
        assertEquals(MergeCompilationContext.ConditionContext.ON, compilationContext.conditionContext());
    }

    @Test
    void onConditionAndMatchConditionManagement() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        when(context.tableRegistry().getOrmTable("items")).thenReturn(null);
        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final MergeCompilationContext compilationContext = new MergeCompilationContext(new MergeNode("items", null), context);
        compilationContext.setUsingNode(new UsingNode(null, "items", null, null));

        // When addOnCondition
        final ConditionNode onCond = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 1);
        compilationContext.addOnCondition(onCond);

        // Then
        assertEquals(1, compilationContext.onConditionGroupStack().current().conditions().size());

        // When addWhenMatchedSpec(true)
        compilationContext.addWhenMatchedSpec(true);
        assertEquals(MergeCompilationContext.ConditionContext.WHEN_MATCHED, compilationContext.conditionContext());

        final ConditionNode matchCond = new ConditionNode(null, LogicOperator.AND, "status", null, Operator.IS_NOT_NULL, null);
        compilationContext.addMatchAndCondition(matchCond);
        assertEquals(1, compilationContext.matchAndConditionGroupStack().current().conditions().size());

        // When addWhenMatchedSpec(false)
        compilationContext.addWhenMatchedSpec(false);
        assertEquals(MergeCompilationContext.ConditionContext.WHEN_NOT_MATCHED, compilationContext.conditionContext());
    }

    @Test
    void whenMatchedUpdateSetVariations() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData nameCol = new ColumnMetaData(table, "user_name", true, Types.VARCHAR, 100);
        final ColumnMetaData ageCol = new ColumnMetaData(table, "age", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("user_name"), List.of(nameCol, ageCol));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.columnMetaDataForField("name")).thenReturn(nameCol);
        when(context.tableRegistry().getOrmTable(UserDto.class)).thenReturn(ormTable);

        final MergeCompilationContext compilationContext = new MergeCompilationContext(new MergeNode(null, UserDto.class), context);
        compilationContext.addWhenMatchedSpec(true);

        // 1. Column by field name in DTO mode
        when(context.mode()).thenReturn(LitebridgeContext.Mode.DTO);
        compilationContext.whenMatchedUpdateSet(new SetNode(null, "name", "Alice"));
        assertEquals(1, compilationContext.getWhenMatchedSpec().getUpdateColumns().size());
        assertEquals(1, compilationContext.getWhenMatchedSpec().getBindValues().size());

        // 2. Column by column name in SQL mode
        when(context.mode()).thenReturn(LitebridgeContext.Mode.SQL);
        compilationContext.whenMatchedUpdateSet(new SetNode(null, "user_name", "Bob"));
        assertEquals(2, compilationContext.getWhenMatchedSpec().getUpdateColumns().size());

        // 3. QueryField
        final QueryField queryField = new QueryField(UserDto.class, "name");
        compilationContext.whenMatchedUpdateSet(new SetNode(null, queryField, "Charlie"));
        assertEquals(3, compilationContext.getWhenMatchedSpec().getUpdateColumns().size());

        // 4. ColumnExpressionSpec
        final SelectColumnSpec colSpec = new SelectColumnSpec(new Column(table, "age"));
        compilationContext.whenMatchedUpdateSet(new SetNode(null, colSpec, 30));
        assertEquals(4, compilationContext.getWhenMatchedSpec().getUpdateColumns().size());

        // 5. Unsupported expression spec
        final SetNode invalidNode = new SetNode(null, new ExpressionSpecArray(new ExpressionSpec[0]), "val");
        assertThrows(IllegalArgumentException.class, () -> compilationContext.whenMatchedUpdateSet(invalidNode));

        // 6. MathOperator throws UnsupportedOperationException
        final SetNode mathNode = new SetNode(null, "user_name", null, 1, MathOperator.ADD);
        assertThrows(UnsupportedOperationException.class, () -> compilationContext.whenMatchedUpdateSet(mathNode));
    }

    @Test
    void whenNotMatchedInsertVariations() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("users");
        final ColumnMetaData nameCol = new ColumnMetaData(table, "user_name", true, Types.VARCHAR, 100);
        final ColumnMetaData ageCol = new ColumnMetaData(table, "age", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("user_name"), List.of(nameCol, ageCol));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.columnMetaDataForField("name")).thenReturn(nameCol);
        when(ormTable.columnMetaDataForField("unknown")).thenReturn(null);
        when(context.tableRegistry().getOrmTable(UserDto.class)).thenReturn(ormTable);

        final MergeCompilationContext compilationContext = new MergeCompilationContext(new MergeNode(null, UserDto.class), context);
        compilationContext.addWhenMatchedSpec(false);

        // 1. Column names in DTO mode
        when(context.mode()).thenReturn(LitebridgeContext.Mode.DTO);
        compilationContext.whenNotMatchedInsert(new InsertNode("users", null, new String[]{"name"}));
        assertEquals(1, compilationContext.getWhenMatchedSpec().getColumnMetaDataList().size());

        // 2. Column names in SQL mode
        when(context.mode()).thenReturn(LitebridgeContext.Mode.SQL);
        compilationContext.whenNotMatchedInsert(new InsertNode("users", null, new String[]{"age"}));
        assertEquals(2, compilationContext.getWhenMatchedSpec().getColumnMetaDataList().size());

        // 3. ExpressionSpecs: ColumnExpressionSpec & QueryField
        final SelectColumnSpec colSpec = new SelectColumnSpec(new Column(table, "age"));
        final QueryField qField = new QueryField(UserDto.class, "name");
        compilationContext.whenNotMatchedInsert(new InsertNode(null, UserDto.class, new ExpressionSpec[]{colSpec, qField}));
        assertEquals(4, compilationContext.getWhenMatchedSpec().getColumnMetaDataList().size());

        // 4. QueryField with unknown field throws IllegalArgumentException
        final QueryField unknownField = new QueryField(UserDto.class, "unknown");
        final InsertNode unknownInsertNode = new InsertNode(null, UserDto.class, new ExpressionSpec[]{unknownField});
        assertThrows(IllegalArgumentException.class, () -> compilationContext.whenNotMatchedInsert(unknownInsertNode));

        // 5. Unsupported expression spec throws UnsupportedOperationException
        final InsertNode unsupportedNode = new InsertNode(null, UserDto.class, new ExpressionSpec[]{new ExpressionSpecArray(new ExpressionSpec[0])});
        assertThrows(UnsupportedOperationException.class, () -> compilationContext.whenNotMatchedInsert(unsupportedNode));

        // 6. Early return when both columns and expressionSpecs are null
        final InsertNode emptyInsertNode = new InsertNode(null, null, null, null, null);
        compilationContext.whenNotMatchedInsert(emptyInsertNode);
    }

    @Test
    void addInsertValuesAppendsBindValues() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        when(context.tableRegistry().getOrmTable("items")).thenReturn(null);
        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final MergeCompilationContext compilationContext = new MergeCompilationContext(new MergeNode("items", null), context);
        compilationContext.addWhenMatchedSpec(false);
        compilationContext.whenNotMatchedInsert(new InsertNode("items", null, new String[]{"id"}));

        // When
        compilationContext.addInsertValues(new InsertValuesNode(null, new Object[]{100}));

        // Then
        assertEquals(1, compilationContext.getWhenMatchedSpec().getBindValues().size());
        assertEquals(new BindValue(100, Types.INTEGER), compilationContext.getWhenMatchedSpec().getBindValues().getFirst());
    }

    @Test
    void addInsertDtoValuesNormalFieldAndForeignKeyAndGenerator() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table userTable = new Table("users");
        final Table roleTable = new Table("roles");

        final ColumnMetaData roleIdCol = new ColumnMetaData(roleTable, "id", false, Types.INTEGER, 0);
        final ForeignKeyConstraint fkc = mock(ForeignKeyConstraint.class);
        when(fkc.foreignKey()).thenReturn(roleIdCol.toColumn());

        final ColumnMetaData userRoleCol = new ColumnMetaData(userTable, "role_id", false, Types.INTEGER, 0);
        userRoleCol.addForeignKeyConstraint(fkc);

        final ColumnMetaData userNameCol = new ColumnMetaData(userTable, "name", true, Types.VARCHAR, 100);

        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);
        final ColumnMetaData userIdCol = new ColumnMetaData(userTable, "id", false, Types.INTEGER, 0, 0, false, null, generator);

        final TableMetaData userMetaData = new TableMetaData(userTable, List.of("id"), List.of(userIdCol, userNameCol, userRoleCol));

        final OrmTable userOrmTable = mock(OrmTable.class);
        when(userOrmTable.getMetaData()).thenReturn(userMetaData);
        when(userOrmTable.mappedColumns()).thenReturn(List.of(userIdCol, userNameCol, userRoleCol));

        final FieldAccessor idAccessor = mock(FieldAccessor.class);
        when(idAccessor.get(any())).thenReturn(null); // id is null, but has generator
        when(userOrmTable.fieldForColumnNameOrNull("id")).thenReturn(idAccessor);

        final FieldAccessor nameAccessor = mock(FieldAccessor.class);
        when(nameAccessor.get(any())).thenReturn("Dave");
        when(nameAccessor.type()).thenReturn((Class) String.class);
        when(userOrmTable.fieldForColumnNameOrNull("name")).thenReturn(nameAccessor);

        final FieldAccessor roleAccessor = mock(FieldAccessor.class);
        final RoleDto roleObj = new RoleDto(99);
        when(roleAccessor.get(any())).thenReturn(roleObj);
        when(roleAccessor.type()).thenReturn((Class) RoleDto.class);
        when(userOrmTable.fieldForColumnNameOrNull("role_id")).thenReturn(roleAccessor);

        final OrmTable roleOrmTable = mock(OrmTable.class);
        final FieldAccessor roleIdAccessor = mock(FieldAccessor.class);
        when(roleIdAccessor.get(roleObj)).thenReturn(99);
        when(roleOrmTable.getFieldForColumnName("id")).thenReturn(roleIdAccessor);
        when(context.tableRegistry().getOrmTableOrThrow(RoleDto.class)).thenReturn(roleOrmTable);

        when(context.tableRegistry().getOrmTable(UserWithRoleDto.class)).thenReturn(userOrmTable);

        final MergeCompilationContext compilationContext = new MergeCompilationContext(new MergeNode(null, UserWithRoleDto.class), context);
        compilationContext.addWhenMatchedSpec(false);

        // When
        final UserWithRoleDto dto = new UserWithRoleDto();
        compilationContext.addInsertDtoValues(new InsertDtoValuesNode(null, dto));

        // Then
        assertEquals(3, compilationContext.getWhenMatchedSpec().getUpdateColumns().size());
        assertEquals(2, compilationContext.getWhenMatchedSpec().getBindValues().size());
    }

    @Test
    void addInsertDtoValuesMissingNonNullableWithoutGeneratorThrowsException() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", false, Types.INTEGER, 0); // NOT NULL, no generator
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(ormTable.mappedColumns()).thenReturn(List.of(idCol));

        final FieldAccessor idAccessor = mock(FieldAccessor.class);
        when(idAccessor.get(any())).thenReturn(null);
        when(ormTable.fieldForColumnNameOrNull("id")).thenReturn(idAccessor);
        when(context.tableRegistry().getOrmTable(UserDto.class)).thenReturn(ormTable);

        final MergeCompilationContext compilationContext = new MergeCompilationContext(new MergeNode(null, UserDto.class), context);
        compilationContext.addWhenMatchedSpec(false);

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> compilationContext.addInsertDtoValues(new InsertDtoValuesNode(null, new UserDto())));
        assertTrue(ex.getMessage().contains("is not nullable and has no generator"));
    }

    @Test
    void toOperationBuildsMergeWithAllClausesAndSequentialBindIndices() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table targetTable = new Table("items");
        final Table sourceTable = new Table("incoming");

        final ColumnMetaData idCol = new ColumnMetaData(targetTable, "id", true, Types.INTEGER, 0);
        final ColumnMetaData valCol = new ColumnMetaData(targetTable, "val", true, Types.INTEGER, 0);
        final TableMetaData targetMeta = new TableMetaData(targetTable, List.of("id"), List.of(idCol, valCol));
        final TableMetaData sourceMeta = new TableMetaData(sourceTable, List.of("id"), List.of(idCol));

        when(context.tableRegistry().getOrmTable("items")).thenReturn(null);
        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(targetTable);
        when(context.tableRegistry().getOrCreateSpiTable("incoming")).thenReturn(sourceTable);
        when(context.tableMetaDataCache().ensureTableMetaData(targetTable)).thenReturn(targetMeta);
        when(context.tableMetaDataCache().ensureTableMetaData(sourceTable)).thenReturn(sourceMeta);

        final ColumnExpression colExpr = mock(ColumnExpression.class);
        when(colExpr.column()).thenReturn(idCol.toColumn());
        when(context.selectExpressionMapper().toSelectExpression(any(), eq(true))).thenReturn(colExpr);
        when(context.typeConverter().convert(any(), eq(Types.INTEGER))).thenAnswer(inv -> inv.getArgument(0));

        final MergeCompilationContext compilationContext = new MergeCompilationContext(new MergeNode("items", null), context);
        compilationContext.setUsingNode(new UsingNode(null, "incoming", null, null));
        compilationContext.addOnCondition(new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 1));

        // When matched: update val = 100
        compilationContext.addWhenMatchedSpec(true);
        compilationContext.whenMatchedUpdateSet(new SetNode(null, "val", 100));

        // When matched: delete
        compilationContext.addWhenMatchedSpec(true);
        compilationContext.getWhenMatchedSpec().setDelete(true);

        // When not matched: insert id = 2, val = 200
        compilationContext.addWhenMatchedSpec(false);
        compilationContext.whenNotMatchedInsert(new InsertNode("items", null, new String[]{"id", "val"}));
        compilationContext.addInsertValues(new InsertValuesNode(null, new Object[]{2, 200}));

        // When
        final Merge merge = (Merge) compilationContext.toOperation();

        // Then
        assertNotNull(merge);
        assertEquals(2, merge.whenMatched().size());
        assertEquals(1, merge.whenNotMatched().size());
        assertEquals(4, compilationContext.getBindValues().size());
    }

    @Test
    void resolveAliasMethods() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        when(context.tableRegistry().getOrmTable("items")).thenReturn(null);
        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(table)).thenReturn(metaData);

        final MergeCompilationContext compilationContext = new MergeCompilationContext(new MergeNode("items", null), context);

        // When & Then
        final Column col = compilationContext.resolveAlias(table, idCol);
        assertNotNull(col);

        final SelectColumnSpec spec = new SelectColumnSpec(new Column(table, "id"));
        final ExpressionSpec resolvedSpec = compilationContext.resolveAlias(spec);
        assertSame(spec, resolvedSpec);

        final ConvertSpec<?> convertSpec = Fn.convert(spec, String.class);
        final ExpressionSpec resolvedConvertSpec = compilationContext.resolveAlias(convertSpec);
        assertSame(convertSpec, resolvedConvertSpec);
    }

    @Test
    void whenMatchedSpecGettersAndSetters() {
        // Given
        final MergeCompilationContext.WhenMatchedSpec spec = new MergeCompilationContext.WhenMatchedSpec(true);

        // Then
        assertTrue(spec.isMatched());
        assertFalse(spec.isDelete());

        // When
        spec.setDelete(true);
        spec.addUpdateColumn(new org.litebridge.db.spi.update.UpdateColumn("col"));

        // Then
        assertTrue(spec.isDelete());
        assertEquals(1, spec.getUpdateColumns().size());
        assertNull(spec.getAndConditionGroupStack());
        assertNotNull(spec.ensureAndConditionGroupStack());
        assertSame(spec.ensureAndConditionGroupStack(), spec.getAndConditionGroupStack());
    }

    static class UserDto {
        private String name;
    }

    static class RoleDto {
        private final int id;
        RoleDto(final int id) { this.id = id; }
    }

    static class UserWithRoleDto {
        private Integer id;
        private String name;
        private RoleDto role;
    }
}
