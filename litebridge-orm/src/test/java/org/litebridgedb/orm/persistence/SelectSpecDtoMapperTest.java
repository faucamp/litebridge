package org.litebridgedb.orm.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.orm.api.dto.DtoJoinSpec;
import org.litebridgedb.orm.api.dto.DtoSelectSpec;
import org.litebridgedb.orm.config.LitebridgeConfig;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.orm.persistence.alias.NoOpAliasGenerator;
import org.litebridgedb.tracking.ChangeTracker;
import org.litebridgedb.tracking.ClassFieldAccessorCache;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelectSpecDtoMapperTest {

    private TypeConverter typeConverter;
    private TableRegistry tableRegistry;
    private DtoConstructor dtoConstructor;
    private LitebridgeContext litebridgeContext;
    private LitebridgeConfig litebridgeConfig;
    private ClassFieldAccessorCache accessorCache;
    private SqlFunctionRegistry sqlFunctionRegistry;
    private FromClauseEngine fromClauseEngine;
    private TransactionalDatabaseProvider databaseProvider;
    private AliasGenerator aliasGenerator;

    @BeforeEach
    void setUp() {
        typeConverter = mock(TypeConverter.class);
        tableRegistry = mock(TableRegistry.class);
        accessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        dtoConstructor = new DtoConstructor(tableRegistry);
        sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        databaseProvider = mock(TransactionalDatabaseProvider.class);
        fromClauseEngine = new FromClauseEngine(databaseProvider, tableRegistry, new ChangeTracker(), dtoConstructor, () -> litebridgeContext);
        aliasGenerator = new NoOpAliasGenerator();

        litebridgeConfig = new LitebridgeConfig();
        litebridgeContext = new LitebridgeContext(litebridgeConfig, fromClauseEngine, sqlFunctionRegistry);
    }

    @Test
    void toDtos_simple() {
        // Given
        final Table table = new Table("TEST_SCHEMA.TEST_TABLE");
        final Column column1 = new Column(table, "ID");
        final Column column2 = new Column(table, "NAME");
        final OrmTable ormTable = mock(OrmTable.class);

        final ColumnMetaData columnMetaData1 = mock(ColumnMetaData.class);
        when(columnMetaData1.name()).thenReturn(column1.name());
        when(columnMetaData1.toColumn()).thenReturn(column1);
        when(ormTable.getColumnMetaData(column1.name())).thenReturn(columnMetaData1);

        final ColumnMetaData columnMetaData2 = mock(ColumnMetaData.class);
        when(columnMetaData2.name()).thenReturn(column2.name());
        when(columnMetaData2.toColumn()).thenReturn(column2);
        when(ormTable.getColumnMetaData(column2.name())).thenReturn(columnMetaData2);

        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(tableMetaData.primaryKey()).thenReturn(List.of(columnMetaData1));
        when(tableMetaData.toTable()).thenReturn(table);
        when(ormTable.getMetaData()).thenReturn(tableMetaData);
        when(ormTable.dtoClass()).thenReturn((Class) SimpleDto.class);
        when(ormTable.getDtoClassInterfaces()).thenReturn(Collections.emptySet());

        when(tableRegistry.getTableOrThrow(SimpleDto.class)).thenReturn(ormTable);
        when(typeConverter.convert(any(), any())).thenAnswer(inv -> inv.getArgument(0));

        final DtoSelectSpec selectSpec = new DtoSelectSpec(SimpleDto.class, ormTable, aliasGenerator, litebridgeContext);
        selectSpec.setExpressions(List.of(
                new SelectFieldSpec(accessorCache.fieldAccessor(SimpleDto.class, "id"), column1),
                new SelectFieldSpec(accessorCache.fieldAccessor(SimpleDto.class, "name"), column2)));

        final SelectSpecDtoMapper mapper = new SelectSpecDtoMapper(selectSpec, typeConverter, tableRegistry, dtoConstructor, litebridgeContext);

        final Row row = new Row()
                .withColumn(column1, 1L)
                .withColumn(column2, "Test");

        // When
        final List<SimpleDto> result = mapper.toDtos(SimpleDto.class, List.of(row));

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Test", result.get(0).getName());
    }

    @Test
    void toDtos_record() {
        // Given
        final Table table = new Table("TEST_SCHEMA.TEST_TABLE");
        final Column column1 = new Column(table, "ID");
        final Column column2 = new Column(table, "NAME");
        final OrmTable ormTable = mock(OrmTable.class);

        final ColumnMetaData columnMetaData1 = mock(ColumnMetaData.class);
        when(columnMetaData1.name()).thenReturn(column1.name());
        when(columnMetaData1.toColumn()).thenReturn(column1);
        when(ormTable.getColumnMetaData(column1.name())).thenReturn(columnMetaData1);

        final ColumnMetaData columnMetaData2 = mock(ColumnMetaData.class);
        when(columnMetaData2.name()).thenReturn(column2.name());
        when(columnMetaData2.toColumn()).thenReturn(column2);
        when(ormTable.getColumnMetaData(column2.name())).thenReturn(columnMetaData2);

        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(tableMetaData.primaryKey()).thenReturn(List.of(columnMetaData1));
        when(tableMetaData.toTable()).thenReturn(table);
        when(ormTable.getMetaData()).thenReturn(tableMetaData);
        when(ormTable.dtoClass()).thenReturn((Class) SimpleRecord.class);
        when(ormTable.getDtoClassInterfaces()).thenReturn(Collections.emptySet());

        when(tableRegistry.getTableOrThrow(SimpleRecord.class)).thenReturn(ormTable);
        when(typeConverter.convert(any(), any())).thenAnswer(inv -> inv.getArgument(0));

        final DtoSelectSpec selectSpec = new DtoSelectSpec(SimpleRecord.class, ormTable, aliasGenerator, litebridgeContext);
        selectSpec.setExpressions(List.of(
                new SelectFieldSpec(accessorCache.fieldAccessor(SimpleRecord.class, "id"), column1),
                new SelectFieldSpec(accessorCache.fieldAccessor(SimpleRecord.class, "name"), column2)));

        final SelectSpecDtoMapper mapper = new SelectSpecDtoMapper(selectSpec, typeConverter, tableRegistry, dtoConstructor, litebridgeContext);

        final Row row = new Row()
                .withColumn(column1, 1L)
                .withColumn(column2, "Test");

        // When
        final List<SimpleRecord> result = mapper.toDtos(SimpleRecord.class, List.of(row));

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Test", result.get(0).name());
    }

    @Test
    void toDtos_nullRow() {
        // Given
        final Table table = new Table("TEST_SCHEMA.TEST_TABLE");
        final Column column1 = new Column(table, "ID");
        final OrmTable ormTable = mock(OrmTable.class);

        final ColumnMetaData columnMetaData1 = mock(ColumnMetaData.class);
        when(columnMetaData1.name()).thenReturn(column1.name());
        when(columnMetaData1.toColumn()).thenReturn(column1);
        when(ormTable.getColumnMetaData(column1.name())).thenReturn(columnMetaData1);

        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(tableMetaData.primaryKey()).thenReturn(List.of(columnMetaData1));
        when(tableMetaData.toTable()).thenReturn(table);
        when(ormTable.getMetaData()).thenReturn(tableMetaData);
        when(ormTable.dtoClass()).thenReturn((Class) SimpleDto.class);
        when(ormTable.getDtoClassInterfaces()).thenReturn(Collections.emptySet());

        when(tableRegistry.getTableOrThrow(SimpleDto.class)).thenReturn(ormTable);

        final DtoSelectSpec selectSpec = new DtoSelectSpec(SimpleDto.class, ormTable, aliasGenerator, litebridgeContext);
        selectSpec.setExpressions(List.of(
                new SelectFieldSpec(accessorCache.fieldAccessor(SimpleDto.class, "id"), column1)));

        final SelectSpecDtoMapper mapper = new SelectSpecDtoMapper(selectSpec, typeConverter, tableRegistry, dtoConstructor, litebridgeContext);

        final Row row = new Row()
                .withColumn(column1, null);

        // When
        final List<SimpleDto> result = mapper.toDtos(SimpleDto.class, List.of(row));

        // Then
        assertEquals(1, result.size());
        assertNull(result.get(0).getId());
    }

    @Test
    void toDtos_emptyFieldColumnsReturnsEmptyList() {
        // Given
        final Table table = new Table("TEST_SCHEMA.TEST_TABLE");
        final OrmTable ormTable = mock(OrmTable.class);
        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(tableMetaData.toTable()).thenReturn(table);
        when(ormTable.getMetaData()).thenReturn(tableMetaData);
        when(ormTable.dtoClass()).thenReturn((Class) SimpleDto.class);
        when(tableRegistry.getTableOrThrow(SimpleDto.class)).thenReturn(ormTable);

        final DtoSelectSpec selectSpec = new DtoSelectSpec(SimpleDto.class, ormTable, aliasGenerator, litebridgeContext);

        final SelectSpecDtoMapper mapper = new SelectSpecDtoMapper(selectSpec, typeConverter, tableRegistry, dtoConstructor, litebridgeContext);
        final Row row = new Row();

        // When
        final List<SimpleDto> result = mapper.toDtos(SimpleDto.class, List.of(row));

        // Then
        assertEquals(0, result.size());
    }

    @Test
    void toDtos_withJoin() {
        // Given
        final Table table = new Table("PARENT_TABLE");
        final Column parentPkCol = new Column(table, "ID");
        final Column parentNameCol = new Column(table, "NAME");
        final Column childRefCol = new Column(table, "CHILD_ID");

        final Table childTable = new Table("CHILD_TABLE");
        final Column childPkCol = new Column(childTable, "ID");

        final OrmTable parentOrmTable = mock(OrmTable.class);
        final OrmTable childOrmTable = mock(OrmTable.class);

        // Parent metadata
        final ColumnMetaData parentPkMD = mock(ColumnMetaData.class);
        when(parentPkMD.name()).thenReturn("ID");
        when(parentPkMD.toColumn()).thenReturn(parentPkCol);
        when(parentOrmTable.getColumnMetaData("ID")).thenReturn(parentPkMD);

        final ColumnMetaData parentNameMD = mock(ColumnMetaData.class);
        when(parentNameMD.name()).thenReturn("NAME");
        when(parentNameMD.toColumn()).thenReturn(parentNameCol);
        when(parentOrmTable.getColumnMetaData("NAME")).thenReturn(parentNameMD);

        final ColumnMetaData childRefMD = mock(ColumnMetaData.class);
        when(childRefMD.name()).thenReturn("CHILD_ID");
        when(childRefMD.toColumn()).thenReturn(childRefCol);
        when(childRefMD.getJoinColumn()).thenReturn("ID");
        when(parentOrmTable.getColumnMetaData("CHILD_ID")).thenReturn(childRefMD);
        when(parentOrmTable.getColumnForFieldName("child")).thenReturn(childRefMD);

        final TableMetaData parentTMD = mock(TableMetaData.class);
        when(parentTMD.primaryKey()).thenReturn(List.of(parentPkMD));
        when(parentTMD.toTable()).thenReturn(table);
        when(parentOrmTable.getMetaData()).thenReturn(parentTMD);
        when(parentOrmTable.dtoClass()).thenReturn((Class) ParentDto.class);
        when(parentOrmTable.getDtoClassInterfaces()).thenReturn(Collections.emptySet());

        // Child metadata
        final ColumnMetaData childPkMD = mock(ColumnMetaData.class);
        when(childPkMD.name()).thenReturn("ID");
        when(childPkMD.toColumn()).thenReturn(childPkCol);
        when(childOrmTable.getColumnMetaData("ID")).thenReturn(childPkMD);
        when(childOrmTable.getFieldForColumnName("ID")).thenReturn(accessorCache.fieldAccessor(ChildDto.class, "id"));

        final TableMetaData childTMD = mock(TableMetaData.class);
        when(childTMD.primaryKey()).thenReturn(List.of(childPkMD));
        when(childTMD.toTable()).thenReturn(childTable);
        when(childOrmTable.getMetaData()).thenReturn(childTMD);
        when(childOrmTable.dtoClass()).thenReturn((Class) ChildDto.class);
        when(childOrmTable.getDtoClassInterfaces()).thenReturn(Collections.emptySet());

        when(tableRegistry.getTableOrThrow(ParentDto.class)).thenReturn(parentOrmTable);
        when(tableRegistry.getTableOrThrow(ChildDto.class)).thenReturn(childOrmTable);
        when(tableRegistry.getTableInContext(ChildDto.class, ParentDto.class)).thenReturn(java.util.Optional.of(childOrmTable));

        when(typeConverter.convert(any(), any())).thenAnswer(inv -> inv.getArgument(0));

        final DtoSelectSpec selectSpec = new DtoSelectSpec(ParentDto.class, parentOrmTable, aliasGenerator, litebridgeContext);
        selectSpec.setProtoExpressionResolver(mock(org.litebridgedb.orm.api.select.model.ProtoExpressionResolver.class));

        // Use setExpressions on selectSpec (which sets fieldColumns)
        selectSpec.setExpressions(List.of(
                new SelectFieldSpec(accessorCache.fieldAccessor(ParentDto.class, "id"), parentPkCol),
                new SelectFieldSpec(accessorCache.fieldAccessor(ParentDto.class, "name"), parentNameCol),
                new SelectFieldSpec(accessorCache.fieldAccessor(ParentDto.class, "child"), childRefCol)));

        // Join
        final DtoJoinSpec joinSpec = selectSpec.newJoinSpec(ChildDto.class, childOrmTable, childTable);
        final DtoSelectSpec.FieldColumn fcChildId = new DtoSelectSpec.FieldColumn(accessorCache.fieldAccessor(ChildDto.class, "id"), childPkCol.as("c_id"));
        joinSpec.setFieldColumns(List.of(fcChildId));

        final SelectSpecDtoMapper mapper = new SelectSpecDtoMapper(selectSpec, typeConverter, tableRegistry, dtoConstructor, litebridgeContext);

        final Column childPkAliased = childPkCol.as("c_id");
        final Row row = new Row()
                .withColumn(parentPkCol, 1L)
                .withColumn(parentNameCol, "Parent")
                .withColumn(childRefCol, 10L)
                .withColumn(childPkAliased, 10L);

        // When
        final List<ParentDto> result = mapper.toDtos(ParentDto.class, List.of(row));

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertNotNull(result.get(0).getChild());
        assertEquals(10L, result.get(0).getChild().getId());
    }

    @Test
    void toDtos_withCollectionMappings() {
        // Given
        final Table table = new Table("PARENT_TABLE");
        final Column parentPkCol = new Column(table, "ID");
        final OrmTable parentOrmTable = mock(OrmTable.class);
        final OrmTable childOrmTable = mock(OrmTable.class);

        final ColumnMetaData parentPkMD = mock(ColumnMetaData.class);
        when(parentPkMD.name()).thenReturn("ID");
        when(parentPkMD.toColumn()).thenReturn(parentPkCol);
        when(parentOrmTable.getColumnMetaData("ID")).thenReturn(parentPkMD);

        final TableMetaData parentTMD = mock(TableMetaData.class);
        when(parentTMD.primaryKey()).thenReturn(List.of(parentPkMD));
        when(parentTMD.toTable()).thenReturn(table);
        when(parentOrmTable.getMetaData()).thenReturn(parentTMD);
        when(parentOrmTable.dtoClass()).thenReturn((Class) ParentWithCollectionsDto.class);
        when(parentOrmTable.getDtoClassInterfaces()).thenReturn(Collections.emptySet());

        // One-to-many mapping
        final org.litebridgedb.tracking.FieldAccessor mappedByField = mock(org.litebridgedb.tracking.FieldAccessor.class);
        final MappedOneToMany otm = new MappedOneToMany(mappedByField, accessorCache.fieldAccessor(ParentWithCollectionsDto.class, "children"));
        when(parentOrmTable.getOneToManyMappings()).thenReturn(List.of(otm));

        // Many-to-many mapping
        final MappedManyToMany mtm = new MappedManyToMany(mock(OrmTable.class), "p_id", accessorCache.fieldAccessor(ParentWithCollectionsDto.class, "others"), new org.litebridgedb.commons.type.ConcurrentLazy<>(() -> childOrmTable), "o_id");
        when(parentOrmTable.getManyToManyMappings()).thenReturn(List.of(mtm));

        // Child table (for collections)
        when(childOrmTable.dtoClass()).thenReturn((Class) ChildDto.class);
        when(tableRegistry.getTableOrThrow(ChildDto.class)).thenReturn(childOrmTable);

        when(tableRegistry.getTableOrThrow(ParentWithCollectionsDto.class)).thenReturn(parentOrmTable);
        when(typeConverter.convert(any(), any())).thenAnswer(inv -> inv.getArgument(0));

        final DtoSelectSpec selectSpec = new DtoSelectSpec(ParentWithCollectionsDto.class, parentOrmTable, aliasGenerator, litebridgeContext);
        selectSpec.setExpressions(List.of(new SelectFieldSpec(accessorCache.fieldAccessor(ParentWithCollectionsDto.class, "id"), parentPkCol)));

        final SelectSpecDtoMapper mapper = new SelectSpecDtoMapper(selectSpec, typeConverter, tableRegistry, dtoConstructor, litebridgeContext);

        final Row row = new Row().withColumn(parentPkCol, 1L);

        // When
        final List<ParentWithCollectionsDto> result = mapper.toDtos(ParentWithCollectionsDto.class, List.of(row));

        // Then
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getChildren());
        assertNotNull(result.get(0).getOthers());
    }

    @Test
    void toDtos_withInterface() {
        // Given
        final Table table = new Table("TEST_SCHEMA.TEST_TABLE");
        final Column column1 = new Column(table, "ID");
        final OrmTable ormTable = mock(OrmTable.class);

        final ColumnMetaData columnMetaData1 = mock(ColumnMetaData.class);
        when(columnMetaData1.name()).thenReturn(column1.name());
        when(columnMetaData1.toColumn()).thenReturn(column1);
        when(ormTable.getColumnMetaData(column1.name())).thenReturn(columnMetaData1);

        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(tableMetaData.primaryKey()).thenReturn(List.of(columnMetaData1));
        when(tableMetaData.toTable()).thenReturn(table);
        when(ormTable.getMetaData()).thenReturn(tableMetaData);
        when(ormTable.dtoClass()).thenReturn((Class) SimpleDto.class);
        when(ormTable.getDtoClassInterfaces()).thenReturn(java.util.Set.of(SimpleInterface.class));

        when(tableRegistry.getTableOrThrow(SimpleDto.class)).thenReturn(ormTable);
        when(typeConverter.convert(any(), any())).thenAnswer(inv -> inv.getArgument(0));

        final DtoSelectSpec selectSpec = new DtoSelectSpec(SimpleDto.class, ormTable, aliasGenerator, litebridgeContext);
        selectSpec.setExpressions(List.of(
                new SelectFieldSpec(accessorCache.fieldAccessor(SimpleDto.class, "id"), column1)));

        final SelectSpecDtoMapper mapper = new SelectSpecDtoMapper(selectSpec, typeConverter, tableRegistry, dtoConstructor, litebridgeContext);

        final Row row = new Row().withColumn(column1, 1L);

        // When
        final List<SimpleInterface> result = mapper.toDtos(SimpleInterface.class, List.of(row));

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof SimpleDto);
    }

    public static class SimpleDto implements SimpleInterface {
        private Long id;
        private String name;

        public void setId(Long id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public interface SimpleInterface {
        Long getId();
    }

    public static class SimpleRecord {
        private Long id;
        private String name;

        public SimpleRecord() {
        }

        public Long id() {
            return id;
        }

        public String name() {
            return name;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

    public static class ParentDto {
        private Long id;
        private String name;
        private ChildDto child;

        public void setId(final Long id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setChild(final ChildDto child) {
            this.child = child;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public ChildDto getChild() {
            return child;
        }
    }

    public static class ChildDto {
        private Long id;
        private String name;

        public void setId(final Long id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static class ParentWithCollectionsDto {
        private Long id;
        private List<ChildDto> children;
        private List<ChildDto> others;

        public void setId(final Long id) {
            this.id = id;
        }

        public void setChildren(final List<ChildDto> children) {
            this.children = children;
        }

        public void setOthers(final List<ChildDto> others) {
            this.others = others;
        }

        public Long getId() {
            return id;
        }

        public List<ChildDto> getChildren() {
            return children;
        }

        public List<ChildDto> getOthers() {
            return others;
        }
    }
}
