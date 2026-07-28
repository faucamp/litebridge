//package org.litebridge.orm.api.dto;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.ColumnMetaData;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.TableMetaData;
//import org.litebridge.db.spi.expression.LiteralExpressionFactory;
//import org.litebridge.db.spi.expression.SelectReferenceExpressionFactory;
//import org.litebridge.db.spi.expression.SqlFunctionRegistry;
//import org.litebridge.orm.engine.FromClauseEngine;
//import org.litebridge.orm.engine.LitebridgeContext;
//import org.litebridge.orm.expression.ExpressionSpec;
//import org.litebridge.orm.expression.ProtoColumnExpressionSpec;
//import org.litebridge.orm.expression.intent.ExpressionSpecArray;
//import org.litebridge.orm.expression.select.SelectFieldSpec;
//import org.litebridge.orm.meta.QueryField;
//import org.litebridge.orm.persistence.DtoConstructor;
//import org.litebridge.orm.persistence.MappedOneToMany;
//import org.litebridge.orm.persistence.OrmTable;
//import org.litebridge.orm.persistence.TableRegistry;
//import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
//import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;
//import org.litebridge.tracking.ClassFieldAccessorCache;
//import org.litebridge.tracking.FieldAccessor;
//
//import java.sql.Types;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class DtoJoinClauseTest {
//
//    @Test
//    @SuppressWarnings("unchecked")
//    void testJoinOn() {
//        final OrmTable ormTable = mock(OrmTable.class);
//        final LitebridgeContext context = mock(LitebridgeContext.class);
//        when(context.fromClauseEngine()).thenReturn(mock(FromClauseEngine.class));
//        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
//        final SqlFunctionRegistry.Select selectFunctions = mock(SqlFunctionRegistry.Select.class);
//        when(sqlFunctionRegistry.select()).thenReturn(selectFunctions);
//        when(selectFunctions.reference()).thenReturn(mock(SelectReferenceExpressionFactory.class));
//        when(selectFunctions.literal()).thenReturn(mock(LiteralExpressionFactory.class));
//        when(context.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
//
//        final Table table = new Table("", null, "TEST", "t1");
//        final TableMetaData metaData = mock(TableMetaData.class);
//        when(ormTable.getMetaData()).thenReturn(metaData);
//        when(metaData.toTable()).thenReturn(table);
//        when(ormTable.dtoClass()).thenReturn((Class) Object.class);
//
//        final ClassFieldAccessorCache cache = mock(ClassFieldAccessorCache.class);
//        final FieldAccessor field1Accessor = mock(FieldAccessor.class);
//        when(field1Accessor.name()).thenReturn("field1");
//        when(cache.fieldAccessorOrThrow(any(), eq("field1"))).thenReturn(field1Accessor);
//
//        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, mock(TableRegistry.class), cache, mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), new NoOpAliasGenerator(), context, null);
//        selector.select();
//
//        final OrmTable joinTable = mock(OrmTable.class);
//        final Table joinSpiTable = new Table("", null, "JOIN_TABLE", "t2");
//        final TableMetaData joinMetaData = mock(TableMetaData.class);
//        when(joinTable.getMetaData()).thenReturn(joinMetaData);
//        when(joinMetaData.toTable()).thenReturn(joinSpiTable);
//        when(joinTable.dtoClass()).thenReturn((Class) String.class);
//
//        final ColumnMetaData joinColMetaData = new ColumnMetaData(table, "JOIN_COL", true, Types.VARCHAR);
//        joinColMetaData.setJoinColumn("ID"); // Points to ID in main table
//        when(ormTable.getColumnForFieldName("field1")).thenReturn(joinColMetaData);
//
//        final ColumnMetaData targetColMetaData = new ColumnMetaData(joinSpiTable, "ID", false, Types.BIGINT);
//        when(joinTable.getColumnMetaData("ID")).thenReturn(targetColMetaData);
//        when(joinMetaData.columns()).thenReturn(List.of(targetColMetaData));
//        when(joinTable.getFieldForColumnName("ID")).thenReturn(mock(FieldAccessor.class));
//
//        // Mock left column in select spec
//        final org.litebridge.db.spi.Column leftColumn = new org.litebridge.db.spi.Column(table, "JOIN_COL");
//        selector.select(new SelectFieldSpec(mock(FieldAccessor.class), leftColumn));
//
//        final DtoJoinClause<Object> joinClause = new DtoJoinClause<>(String.class, joinTable, selector);
//
//        final DtoJoinConditionClauseTerminal<Object> terminal = joinClause.on("field1");
//        assertNotNull(terminal);
//    }
//
//    @Test
//    @SuppressWarnings("unchecked")
//    void testJoinOneToMany() {
//        final OrmTable ormTable = mock(OrmTable.class);
//        final LitebridgeContext context = mock(LitebridgeContext.class);
//        when(context.fromClauseEngine()).thenReturn(mock(FromClauseEngine.class));
//        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
//        final SqlFunctionRegistry.Select selectFunctions = mock(SqlFunctionRegistry.Select.class);
//        when(sqlFunctionRegistry.select()).thenReturn(selectFunctions);
//        when(selectFunctions.reference()).thenReturn(mock(SelectReferenceExpressionFactory.class));
//        when(context.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
//
//        final Table table = new Table("", null, "TEST", "t1");
//        final TableMetaData metaData = mock(TableMetaData.class);
//        when(ormTable.getMetaData()).thenReturn(metaData);
//        when(metaData.toTable()).thenReturn(table);
//        when(ormTable.dtoClass()).thenReturn((Class) Object.class);
//
//        final ClassFieldAccessorCache cache = mock(ClassFieldAccessorCache.class);
//        final FieldAccessor field1Accessor = mock(FieldAccessor.class);
//        when(field1Accessor.name()).thenReturn("field1");
//        when(cache.fieldAccessorOrThrow(any(), eq("field1"))).thenReturn(field1Accessor);
//
//        final MappedOneToMany oneToMany = mock(MappedOneToMany.class);
//        final FieldAccessor mappedByField = mock(FieldAccessor.class);
//        when(mappedByField.name()).thenReturn("ownerId");
//        when(oneToMany.mappedByField()).thenReturn(mappedByField);
//        when(ormTable.getOneToManyMappingForField(field1Accessor)).thenReturn(Optional.of(oneToMany));
//
//        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, mock(TableRegistry.class), cache, mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), new NoOpAliasGenerator(), context, null);
//        selector.select();
//
//        final OrmTable joinTable = mock(OrmTable.class);
//        final Table joinSpiTable = new Table("", null, "JOIN_TABLE", "t2");
//        final TableMetaData joinMetaData = mock(TableMetaData.class);
//        when(joinTable.getMetaData()).thenReturn(joinMetaData);
//        when(joinMetaData.toTable()).thenReturn(joinSpiTable);
//
//        final ColumnMetaData ownerIdCol = new ColumnMetaData(joinSpiTable, "OWNER_ID", true, Types.BIGINT);
//        ownerIdCol.setJoinColumn("ID");
//        when(joinTable.getColumnForFieldName("ownerId")).thenReturn(ownerIdCol);
//
//        final ColumnMetaData idCol = new ColumnMetaData(joinSpiTable, "ID", false, Types.BIGINT);
//        when(joinTable.getColumnMetaData("ID")).thenReturn(idCol);
//        when(joinMetaData.columns()).thenReturn(List.of(ownerIdCol, idCol));
//
//        when(joinTable.getFieldForColumnName("OWNER_ID")).thenReturn(mappedByField);
//        when(joinTable.getFieldForColumnName("ID")).thenReturn(mock(FieldAccessor.class));
//
//        // Mock left column
//        final Column leftColumn = new Column(joinSpiTable, "OWNER_ID");
//        selector.select(new SelectFieldSpec(mock(FieldAccessor.class), leftColumn));
//
//        final DtoJoinClause<Object> joinClause = new DtoJoinClause<>(String.class, joinTable, selector);
//        assertNotNull(joinClause.on("field1"));
//    }
//
//    @Test
//    @SuppressWarnings("unchecked")
//    void testJoinOnExpressions() {
//        final OrmTable ormTable = mock(OrmTable.class);
//        final LitebridgeContext context = mock(LitebridgeContext.class);
//        when(context.fromClauseEngine()).thenReturn(mock(FromClauseEngine.class));
//        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
//        final SqlFunctionRegistry.Select selectFunctions = mock(SqlFunctionRegistry.Select.class);
//        when(sqlFunctionRegistry.select()).thenReturn(selectFunctions);
//        when(selectFunctions.reference()).thenReturn(mock(SelectReferenceExpressionFactory.class));
//        when(selectFunctions.literal()).thenReturn(mock(LiteralExpressionFactory.class));
//        when(context.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
//
//        final Table table = new Table("", null, "TEST", "t1");
//        final TableMetaData metaData = mock(TableMetaData.class);
//        when(ormTable.getMetaData()).thenReturn(metaData);
//        when(metaData.toTable()).thenReturn(table);
//        when(ormTable.dtoClass()).thenReturn((Class) Object.class);
//
//        final ClassFieldAccessorCache cache = mock(ClassFieldAccessorCache.class);
//        final FieldAccessor field1Accessor = mock(FieldAccessor.class);
//        when(field1Accessor.name()).thenReturn("field1");
//        when(cache.fieldAccessorOrThrow(any(), eq("field1"))).thenReturn(field1Accessor);
//
//        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, mock(TableRegistry.class), cache, mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), new NoOpAliasGenerator(), context, null);
//        selector.select();
//
//        final OrmTable joinTable = mock(OrmTable.class);
//        final Table joinSpiTable = new Table("", null, "JOIN_TABLE", "t2");
//        final TableMetaData joinMetaData = mock(TableMetaData.class);
//        when(joinTable.getMetaData()).thenReturn(joinMetaData);
//        when(joinMetaData.toTable()).thenReturn(joinSpiTable);
//        when(joinTable.dtoClass()).thenReturn((Class) String.class);
//
//        final ColumnMetaData joinColMetaData = new ColumnMetaData(table, "JOIN_COL", true, Types.VARCHAR);
//        joinColMetaData.setJoinColumn("ID");
//        when(ormTable.getColumnForFieldName("field1")).thenReturn(joinColMetaData);
//
//        final ColumnMetaData targetColMetaData = new ColumnMetaData(joinSpiTable, "ID", false, Types.BIGINT);
//        when(joinTable.getColumnMetaData("ID")).thenReturn(targetColMetaData);
//        when(joinMetaData.columns()).thenReturn(List.of(targetColMetaData));
//        when(joinTable.getFieldForColumnName("ID")).thenReturn(mock(FieldAccessor.class));
//
//        // Mock left column
//        final Column leftColumn = new Column(table, "JOIN_COL");
//        selector.select(new SelectFieldSpec(mock(FieldAccessor.class), leftColumn));
//
//        final DtoJoinClause<Object> joinClause = new DtoJoinClause<>(String.class, joinTable, selector);
//
//        // QueryField
//        final QueryField qf = new QueryField(Object.class, "field1");
//        assertNotNull(joinClause.on(qf));
//
//        // ProtoExpressionSpec
//        final ProtoColumnExpressionSpec proto = new ProtoColumnExpressionSpec(SelectFieldSpec.class, "field1");
//        assertNotNull(joinClause.on(proto));
//
//        // SelectFieldSpec
//        final SelectFieldSpec sfs = new SelectFieldSpec(field1Accessor, leftColumn);
//        assertNotNull(joinClause.on(sfs));
//
//        // Unsupported
//        assertThrows(IllegalArgumentException.class, () -> joinClause.on(new ExpressionSpecArray(new ExpressionSpec[0])));
//    }
//}
