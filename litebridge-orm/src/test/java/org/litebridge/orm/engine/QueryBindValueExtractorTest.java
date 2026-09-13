package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.ForeignKeyConstraint;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.generator.ColumnValueGenerator;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.ConditionWithIdNode;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.InsertDtoValuesNode;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.engine.ast.UpdateNode;
import org.litebridge.orm.engine.ast.UsingNode;
import org.litebridge.orm.engine.ast.WhenMatchedNode;
import org.litebridge.orm.engine.ast.WhenNotMatchedNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.tracking.FieldAccessor;
import org.mockito.MockedStatic;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class QueryBindValueExtractorTest {

    @Test
    void extractBindValues_whereValuesInConditionOrder() {
        // Given
        final ConditionNode first = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, "first");
        final ConditionNode last = new ConditionNode(first, LogicOperator.AND, null, null, Operator.GT, 10);

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(new WhereNode(null, last), mock(LitebridgeContext.class));

        // Then
        assertEquals(List.of("first", 10), result);
    }

    @Test
    void extractBindValues_havingAndJoinValues() {
        // Given
        final ConditionNode havingCondition = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, "having");
        final HavingNode having = new HavingNode(null, havingCondition);
        final JoinNode joinWithoutCondition = new JoinNode(having, "INNER", Object.class, "other");
        final ConditionNode joinCondition = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, "join");
        final JoinNode joinNode = new JoinNode(joinWithoutCondition, "INNER", Object.class, "other");
        joinNode.setCondition(joinCondition);

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(joinNode, mock(LitebridgeContext.class));

        // Then
        assertEquals(List.of("having", "join"), result);
    }

    @Test
    void extractsCollectionsAndConditionIdsButSkipsOperatorsAndExpressions() {
        // Given
        final ConditionNode skippedNull = new ConditionNode(null, LogicOperator.AND, null, null, Operator.IS_NULL, null);
        final ConditionNode skippedNotNull = new ConditionNode(skippedNull, LogicOperator.AND, null, null, Operator.IS_NOT_NULL, null);
        final ConditionNode skippedUsing = new ConditionNode(skippedNotNull, LogicOperator.AND, null, null, Operator.USING, "column");
        final ConditionNode expression = new ConditionNode(skippedUsing, LogicOperator.AND, null, null, Operator.EQ, mock(Column.class));
        final ConditionNode expressionSpec = new ConditionNode(expression, LogicOperator.AND, null, null, Operator.EQ, new SelectColumnSpec(new Column("t", "test")));
        final ConditionNode values = new ConditionNode(expressionSpec, LogicOperator.AND, null, null, Operator.IN, List.of("a", "b"));
        final ConditionWithIdNode id = new ConditionWithIdNode(values, LogicOperator.AND, Operator.EQ, 42L);

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(new WhereNode(null, id), mock(LitebridgeContext.class));

        // Then
        assertEquals(List.of("a", "b", 42L), result);
    }

    @Test
    void extractBindValues_valuesFromNestedQueryNodesAndSelectTerminals() {
        // Given
        final ConditionNode nestedCondition = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, "nested");
        final WhereNode nestedQuery = new WhereNode(null, nestedCondition);
        final ConditionNode queryRhs = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, nestedQuery);
        assertEquals(List.of("nested"), QueryBindValueExtractor.extractBindValues(new WhereNode(null, queryRhs), mock(LitebridgeContext.class)));

        final SelectTerminal<?> terminal = mock(SelectTerminal.class);
        try (MockedStatic<SelectTerminalInspector> inspector = mockStatic(SelectTerminalInspector.class)) {
            inspector.when(() -> SelectTerminalInspector.getNode(terminal)).thenReturn(nestedQuery);
            final ConditionNode terminalRhs = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, terminal);

            // When
            final List<Object> result = QueryBindValueExtractor.extractBindValues(new WhereNode(null, terminalRhs), mock(LitebridgeContext.class));

            // Then
            assertEquals(List.of("nested"), result);
        }
    }

    @Test
    void extractBindValues_conditionsBeforeNestedGroupsAndInsertRows() {
        // Given
        final ConditionNode first = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, "first");
        final ConditionNode nested = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, "nested");
        final ConditionGroupNode group = new ConditionGroupNode(first, LogicOperator.OR, nested);
        final ConditionNode last = new ConditionNode(group, LogicOperator.AND, null, null, Operator.EQ, "last");
        final WhereNode where = new WhereNode(null, last);
        assertEquals(List.of("first", "last", "nested"), QueryBindValueExtractor.extractBindValues(where, mock(LitebridgeContext.class)));

        final InsertNode insert = new InsertNode("table", Object.class, new String[]{"a", "b"});
        final InsertValuesNode values = new InsertValuesNode(insert, new Object[]{"row-a", null});

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(values, mock(LitebridgeContext.class));

        // Then
        assertEquals(Arrays.asList("row-a", null), result);
    }

    private record AccountRecord(Long id, String name, Integer balance) {
    }

    @Test
    void extractBindValues_mergeNode_sqlMode() {
        // Given
        final MergeNode root = new MergeNode("target", null);
        final ConditionNode onCondition = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, 1);
        final UsingNode using = new UsingNode(root, "source", null, onCondition);

        final UpdateNode update = new UpdateNode(null, "target", null);
        final SetNode set = new SetNode(update, "balance", 500);
        final ConditionNode whereCondition = new ConditionNode(null, LogicOperator.AND, null, null, Operator.LT, 5);
        final WhereNode where = new WhereNode(set, whereCondition);
        final WhenMatchedNode matched1 = new WhenMatchedNode(using, where);

        final DeleteNode delete = new DeleteNode(null, "target", null);
        final ConditionNode deleteCondition = new ConditionNode(null, LogicOperator.AND, null, null, Operator.GTE, 10);
        final WhereNode deleteWhere = new WhereNode(delete, deleteCondition);
        final WhenMatchedNode matched2 = new WhenMatchedNode(matched1, deleteWhere);

        final InsertNode insert = new InsertNode("target", null, new String[]{"id", "balance"});
        final InsertValuesNode insertValues = new InsertValuesNode(insert, new Object[]{123, 0});
        final WhenNotMatchedNode notMatched = new WhenNotMatchedNode(matched2, null, insertValues);

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(notMatched, mock(LitebridgeContext.class));

        // Then
        assertEquals(List.of(1, 5, 500, 10, 123, 0), result);
    }

    @Test
    void extractBindValues_mergeNode_dtoMode() {
        // Given
        final MergeNode root = new MergeNode("target", null);
        final ConditionNode onCondition = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, 1);
        final UsingNode using = new UsingNode(root, "source", null, onCondition);

        final InsertNode insertNode = new InsertNode(null, AccountRecord.class, null, null, null);
        final InsertDtoValuesNode insertDto = new InsertDtoValuesNode(insertNode, new AccountRecord(456L, "Test", 100));
        final WhenNotMatchedNode notMatched = new WhenNotMatchedNode(using, null, insertDto);

        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        when(litebridgeContext.tableRegistry()).thenReturn(new TableRegistry());

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(notMatched, litebridgeContext);

        // Then
        assertEquals(List.of(1, 456L, "Test", 100), result);
    }

    static class ParentDto {
        Long id = 999L;
    }

    static class SampleDto {
        ParentDto parent = new ParentDto();
        String name = "Alice";
        Long autoId = null;
        Long genId = null;
        String note = null;
    }

    private record ThrowingRecord(String field) {
        @Override
        public String field() {
            throw new RuntimeException("boom");
        }
    }

    @Test
    void extractDtoValues_withOrmTableMappingsAndForeignKeys() {
        // Given
        final Table table = new Table("test_table");
        final Table parentTable = new Table("parent_table");

        final ColumnMetaData col1Fk = new ColumnMetaData(table, "parent_id", false, java.sql.Types.BIGINT);
        final ForeignKeyConstraint fkc = new ForeignKeyConstraint("fk_parent", new Column(parentTable, "id"));
        col1Fk.addForeignKeyConstraint(fkc);

        final ColumnMetaData col2Unmapped = new ColumnMetaData(table, "unmapped", false, java.sql.Types.VARCHAR);
        final ColumnMetaData col3Normal = new ColumnMetaData(table, "name", false, java.sql.Types.VARCHAR);

        final ColumnMetaData col4AutoInc = new ColumnMetaData(table, "auto_id", false, java.sql.Types.BIGINT, 0, 0, true, null, null);

        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);
        final ColumnMetaData col5Generated = new ColumnMetaData(table, "gen_id", false, java.sql.Types.BIGINT, 0, 0, false, null, generator);

        final ColumnMetaData col6Nullable = new ColumnMetaData(table, "note", true, java.sql.Types.VARCHAR);

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.mappedColumns()).thenReturn(List.of(col1Fk, col2Unmapped, col3Normal, col4AutoInc, col5Generated, col6Nullable));

        final FieldAccessor parentAccessor = mock(FieldAccessor.class);
        when(parentAccessor.type()).thenReturn((Class) ParentDto.class);
        final ParentDto parentDto = new ParentDto();
        final SampleDto sampleDto = new SampleDto();
        sampleDto.parent = parentDto;
        when(parentAccessor.get(sampleDto)).thenReturn(parentDto);
        when(ormTable.fieldForColumnNameOrNull("parent_id")).thenReturn(parentAccessor);

        when(ormTable.fieldForColumnNameOrNull("unmapped")).thenReturn(null);

        final FieldAccessor nameAccessor = mock(FieldAccessor.class);
        when(nameAccessor.type()).thenReturn((Class) String.class);
        when(nameAccessor.get(sampleDto)).thenReturn("Alice");
        when(ormTable.fieldForColumnNameOrNull("name")).thenReturn(nameAccessor);

        final FieldAccessor autoAccessor = mock(FieldAccessor.class);
        when(autoAccessor.get(sampleDto)).thenReturn(null);
        when(ormTable.fieldForColumnNameOrNull("auto_id")).thenReturn(autoAccessor);

        final FieldAccessor genAccessor = mock(FieldAccessor.class);
        when(genAccessor.get(sampleDto)).thenReturn(null);
        when(ormTable.fieldForColumnNameOrNull("gen_id")).thenReturn(genAccessor);

        final FieldAccessor noteAccessor = mock(FieldAccessor.class);
        when(noteAccessor.get(sampleDto)).thenReturn(null);
        when(ormTable.fieldForColumnNameOrNull("note")).thenReturn(noteAccessor);

        final OrmTable fkOrmTable = mock(OrmTable.class);
        final FieldAccessor fkIdAccessor = mock(FieldAccessor.class);
        when(fkIdAccessor.get(parentDto)).thenReturn(999L);
        when(fkOrmTable.getFieldForColumnName("id")).thenReturn(fkIdAccessor);

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getOrmTable(SampleDto.class)).thenReturn(ormTable);
        when(tableRegistry.getOrmTableOrThrow(ParentDto.class)).thenReturn(fkOrmTable);

        final LitebridgeContext context = mock(LitebridgeContext.class);
        when(context.tableRegistry()).thenReturn(tableRegistry);

        final InsertNode insertNode = new InsertNode(null, SampleDto.class, null, null, null);
        final InsertDtoValuesNode insertDtoValuesNode = new InsertDtoValuesNode(insertNode, sampleDto);

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(insertDtoValuesNode, context);

        // Then
        assertEquals(Arrays.asList(999L, "Alice", null), result);
    }

    @Test
    void extractDtoValues_throwingRecordComponentHandledGracefully() {
        // Given
        final ThrowingRecord record = new ThrowingRecord("test");
        final InsertNode insertNode = new InsertNode(null, ThrowingRecord.class, null, null, null);
        final InsertDtoValuesNode insertDto = new InsertDtoValuesNode(insertNode, record);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        when(context.tableRegistry()).thenReturn(new TableRegistry());

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(insertDto, context);

        // Then
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void extractBindValues_whenNotMatchedWithAndClause() {
        // Given
        final MergeNode root = new MergeNode("target", null);
        final ConditionNode onCondition = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, 1);
        final UsingNode using = new UsingNode(root, "source", null, onCondition);

        final ConditionNode andCondition = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, 777);
        final InsertNode insert = new InsertNode("target", null, new String[]{"id"});
        final InsertValuesNode insertValues = new InsertValuesNode(insert, new Object[]{888});
        final WhenNotMatchedNode notMatched = new WhenNotMatchedNode(using, andCondition, insertValues);

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(notMatched, mock(LitebridgeContext.class));

        // Then
        assertEquals(List.of(1, 777, 888), result);
    }

    @Test
    void extractDtoValues_nonRecordNoOrmTable() {
        // Given
        final Object nonRecord = new Object();
        final InsertNode insertNode = new InsertNode(null, Object.class, null, null, null);
        final InsertDtoValuesNode insertDto = new InsertDtoValuesNode(insertNode, nonRecord);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        when(context.tableRegistry()).thenReturn(new TableRegistry());

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(insertDto, context);

        // Then
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void extractDtoValues_foreignKeyConstraintWithBasicType() {
        // Given
        final Table table = new Table("test_table");
        final ColumnMetaData colFk = new ColumnMetaData(table, "user_id", false, java.sql.Types.BIGINT);
        final ForeignKeyConstraint fkc = new ForeignKeyConstraint("fk_user", new Column(new Table("users"), "id"));
        colFk.addForeignKeyConstraint(fkc);

        final OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.mappedColumns()).thenReturn(List.of(colFk));

        final FieldAccessor fieldAccessor = mock(FieldAccessor.class);
        when(fieldAccessor.type()).thenReturn((Class) Long.class);
        when(fieldAccessor.get(any())).thenReturn(100L);
        when(ormTable.fieldForColumnNameOrNull("user_id")).thenReturn(fieldAccessor);

        final TableRegistry tableRegistry = mock(TableRegistry.class);
        when(tableRegistry.getOrmTable(SampleDto.class)).thenReturn(ormTable);

        final LitebridgeContext context = mock(LitebridgeContext.class);
        when(context.tableRegistry()).thenReturn(tableRegistry);

        final InsertNode insertNode = new InsertNode(null, SampleDto.class, null, null, null);
        final InsertDtoValuesNode insertDto = new InsertDtoValuesNode(insertNode, new SampleDto());

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(insertDto, context);

        // Then
        assertEquals(List.of(100L), result);
    }

    @Test
    void extractBindValues_whenNotMatchedWithInsertDtoValuesNode() {
        // Given
        final MergeNode root = new MergeNode("target", null);
        final ConditionNode onCondition = new ConditionNode(null, LogicOperator.AND, null, null, Operator.EQ, 1);
        final UsingNode using = new UsingNode(root, "source", null, onCondition);

        final InsertNode insert = new InsertNode("target", null, new String[]{"id"});
        final InsertDtoValuesNode insertDto = new InsertDtoValuesNode(insert, new AccountRecord(77L, "name", 10));
        final WhenNotMatchedNode notMatched = new WhenNotMatchedNode(using, null, insertDto);

        final LitebridgeContext context = mock(LitebridgeContext.class);
        when(context.tableRegistry()).thenReturn(new TableRegistry());

        // When
        final List<Object> result = QueryBindValueExtractor.extractBindValues(notMatched, context);

        // Then
        assertEquals(List.of(1, 77L, "name", 10), result);
    }

    @Test
    void testPrivateConstructor() throws Exception {
        // Given
        final Constructor<QueryBindValueExtractor> constructor = QueryBindValueExtractor.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // When
        final QueryBindValueExtractor instance = constructor.newInstance();

        // Then
        assertNotNull(instance);
    }
}
