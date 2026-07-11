package org.litebridgedb.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.expression.LiteralExpressionFactory;
import org.litebridgedb.db.spi.expression.SelectReferenceExpressionFactory;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.persistence.DtoConstructor;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.orm.persistence.alias.NoOpAliasGenerator;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.FieldAccessor;

import java.sql.Types;
import org.litebridgedb.orm.persistence.MappedManyToMany;
import org.litebridgedb.orm.persistence.MappedOneToMany;
import org.litebridgedb.commons.type.ConcurrentLazy;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoJoinClauseTest {

    @Test
    @SuppressWarnings("unchecked")
    void testJoinOn() {
        final OrmTable ormTable = mock(OrmTable.class);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        when(context.fromClauseEngine()).thenReturn(mock(FromClauseEngine.class));
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectFunctions = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectFunctions);
        when(selectFunctions.reference()).thenReturn(mock(SelectReferenceExpressionFactory.class));
        when(selectFunctions.literal()).thenReturn(mock(LiteralExpressionFactory.class));
        when(context.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);

        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) Object.class);

        final ClassFieldAccessorCache cache = mock(ClassFieldAccessorCache.class);
        final FieldAccessor field1Accessor = mock(FieldAccessor.class);
        when(field1Accessor.name()).thenReturn("field1");
        when(cache.fieldAccessorOrThrow(any(), eq("field1"))).thenReturn(field1Accessor);

        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, mock(TableRegistry.class), cache, mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), new NoOpAliasGenerator(), context);
        selector.select();

        final OrmTable joinTable = mock(OrmTable.class);
        final Table joinSpiTable = new Table("", null, "JOIN_TABLE", "t2");
        final TableMetaData joinMetaData = mock(TableMetaData.class);
        when(joinTable.getMetaData()).thenReturn(joinMetaData);
        when(joinMetaData.toTable()).thenReturn(joinSpiTable);
        when(joinTable.dtoClass()).thenReturn((Class) String.class);
        
        final ColumnMetaData joinColMetaData = new ColumnMetaData(table, "JOIN_COL", true, Types.VARCHAR);
        joinColMetaData.setJoinColumn("ID"); // Points to ID in main table
        when(ormTable.getColumnForFieldName("field1")).thenReturn(joinColMetaData);
        
        final ColumnMetaData targetColMetaData = new ColumnMetaData(joinSpiTable, "ID", false, Types.BIGINT);
        when(joinTable.getColumnMetaData("ID")).thenReturn(targetColMetaData);
        when(joinMetaData.columns()).thenReturn(List.of(targetColMetaData));
        when(joinTable.getFieldForColumnName("ID")).thenReturn(mock(FieldAccessor.class));

        // Mock left column in select spec
        final org.litebridgedb.db.spi.Column leftColumn = new org.litebridgedb.db.spi.Column(table, "JOIN_COL");
        selector.select(new SelectFieldSpec(mock(FieldAccessor.class), leftColumn));

        final DtoJoinClause<Object> joinClause = new DtoJoinClause<>(String.class, joinTable, selector);
        
        final DtoJoinConditionClauseTerminal<Object> terminal = joinClause.on("field1");
        assertNotNull(terminal);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testJoinOneToMany() {
        final OrmTable ormTable = mock(OrmTable.class);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        when(context.fromClauseEngine()).thenReturn(mock(FromClauseEngine.class));
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectFunctions = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectFunctions);
        when(selectFunctions.reference()).thenReturn(mock(SelectReferenceExpressionFactory.class));
        when(context.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);

        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) Object.class);

        final ClassFieldAccessorCache cache = mock(ClassFieldAccessorCache.class);
        final FieldAccessor field1Accessor = mock(FieldAccessor.class);
        when(field1Accessor.name()).thenReturn("field1");
        when(cache.fieldAccessorOrThrow(any(), eq("field1"))).thenReturn(field1Accessor);

        final MappedOneToMany oneToMany = mock(MappedOneToMany.class);
        final FieldAccessor mappedByField = mock(FieldAccessor.class);
        when(mappedByField.name()).thenReturn("ownerId");
        when(oneToMany.mappedByField()).thenReturn(mappedByField);
        when(ormTable.getOneToManyMappingForField(field1Accessor)).thenReturn(Optional.of(oneToMany));

        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, mock(TableRegistry.class), cache, mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), new NoOpAliasGenerator(), context);
        selector.select();

        final OrmTable joinTable = mock(OrmTable.class);
        final Table joinSpiTable = new Table("", null, "JOIN_TABLE", "t2");
        final TableMetaData joinMetaData = mock(TableMetaData.class);
        when(joinTable.getMetaData()).thenReturn(joinMetaData);
        when(joinMetaData.toTable()).thenReturn(joinSpiTable);
        
        final ColumnMetaData ownerIdCol = new ColumnMetaData(joinSpiTable, "OWNER_ID", true, Types.BIGINT);
        ownerIdCol.setJoinColumn("ID");
        when(joinTable.getColumnForFieldName("ownerId")).thenReturn(ownerIdCol);
        
        final ColumnMetaData idCol = new ColumnMetaData(joinSpiTable, "ID", false, Types.BIGINT);
        when(joinTable.getColumnMetaData("ID")).thenReturn(idCol);
        when(joinMetaData.columns()).thenReturn(List.of(ownerIdCol, idCol));
        
        when(joinTable.getFieldForColumnName("OWNER_ID")).thenReturn(mappedByField);
        when(joinTable.getFieldForColumnName("ID")).thenReturn(mock(FieldAccessor.class));

        // Mock left column
        final org.litebridgedb.db.spi.Column leftColumn = new org.litebridgedb.db.spi.Column(joinSpiTable, "OWNER_ID");
        selector.select(new SelectFieldSpec(mock(FieldAccessor.class), leftColumn));

        final DtoJoinClause<Object> joinClause = new DtoJoinClause<>(String.class, joinTable, selector);
        assertNotNull(joinClause.on("field1"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testJoinManyToMany() {
        final OrmTable ormTable = mock(OrmTable.class);
        final LitebridgeContext context = mock(LitebridgeContext.class);
        when(context.fromClauseEngine()).thenReturn(mock(FromClauseEngine.class));
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectFunctions = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectFunctions);
        when(selectFunctions.reference()).thenReturn(mock(SelectReferenceExpressionFactory.class));
        when(context.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);

        final Table table = new Table("", null, "TEST", "t1");
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(table);
        when(ormTable.dtoClass()).thenReturn((Class) Object.class);

        final ClassFieldAccessorCache cache = mock(ClassFieldAccessorCache.class);
        final FieldAccessor field1Accessor = mock(FieldAccessor.class);
        when(field1Accessor.name()).thenReturn("field1");
        when(cache.fieldAccessorOrThrow(any(), eq("field1"))).thenReturn(field1Accessor);

        final MappedManyToMany mtm = mock(MappedManyToMany.class);
        final OrmTable intermediateOrmTable = mock(OrmTable.class);
        final Table intermediateSpiTable = new Table("", null, "JOIN_TABLE", "t2");
        when(intermediateOrmTable.getMetaData()).thenReturn(mock(TableMetaData.class));
        when(intermediateOrmTable.getMetaData().toTable()).thenReturn(intermediateSpiTable);
        
        when(mtm.joinTable()).thenReturn(intermediateOrmTable);
        when(mtm.joinColumn()).thenReturn("ID_LEFT");
        when(mtm.inverseJoinColumn()).thenReturn("ID_RIGHT");
        
        final OrmTable targetOrmTable = mock(OrmTable.class);
        final ConcurrentLazy<OrmTable> targetResolvable = new ConcurrentLazy<>(() -> targetOrmTable);
        when(mtm.targetTable()).thenReturn(targetResolvable);
        
        when(ormTable.getManyToManyMappingForField(field1Accessor)).thenReturn(Optional.of(mtm));
        when(ormTable.getMetaData().primaryKey()).thenReturn(List.of(new ColumnMetaData(table, "ID", false, Types.BIGINT)));

        final DtoSelector<Object> selector = new DtoSelector<>(Object.class, ormTable, mock(TableRegistry.class), cache, mock(DtoConstructor.class), mock(TransactionalDatabaseProvider.class), new NoOpAliasGenerator(), context);
        selector.select();

        final OrmTable joinTable = mock(OrmTable.class);
        final Table joinSpiTable = new Table("", null, "TARGET_TABLE", "t3");
        when(joinTable.getMetaData()).thenReturn(mock(TableMetaData.class));
        when(joinTable.getMetaData().toTable()).thenReturn(joinSpiTable);

        // Required by createIntermediateJoinSpec
        when(intermediateOrmTable.getColumnMetaData("ID_LEFT")).thenReturn(new ColumnMetaData(intermediateSpiTable, "ID_LEFT", false, Types.BIGINT));
        // Required by manyToManyJoin
        when(intermediateOrmTable.getColumnMetaData("ID_RIGHT")).thenReturn(new ColumnMetaData(intermediateSpiTable, "ID_RIGHT", false, Types.BIGINT));
        when(targetOrmTable.getColumnMetaData("ID_RIGHT")).thenReturn(new ColumnMetaData(joinSpiTable, "ID_RIGHT", false, Types.BIGINT));

        final DtoJoinClause<Object> joinClause = new DtoJoinClause<>(String.class, joinTable, selector);
        assertNotNull(joinClause.on("field1"));
    }
}
