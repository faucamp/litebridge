package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.RowColumn;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

import java.lang.invoke.MethodHandles;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoMapperTest {

    @Test
    void toDtos_emptyRows() {
        // Given
        final DtoMapper dtoMapper = createDtoMapper(new TableRegistry());

        // When
        final List<TestPersonDto> result = dtoMapper.toDtos(TestPersonDto.class, null, Collections.emptyList());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void toDtos_unqualifiedTableInRowColumn() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table canonicalTable = new Table("", "public", "test_person");
        final ColumnMetaData idCol = new ColumnMetaData(canonicalTable, "id", false, Types.BIGINT);
        final ColumnMetaData nameCol = new ColumnMetaData(canonicalTable, "name", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(canonicalTable, List.of("id"), List.of(idCol, nameCol));
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);
        final FieldAccessor idField = cache.fieldAccessorOrThrow(TestPersonDto.class, "id");
        final FieldAccessor nameField = cache.fieldAccessorOrThrow(TestPersonDto.class, "name");
        final OrmTable ormTable = new OrmTable(
                TestPersonDto.class,
                metaData,
                Map.of(idField, idCol, nameField, nameCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(TestPersonDto.class, ormTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);

        // RowColumn has an unqualified Table (catalog=null, schema=null) as returned by Postgres JDBC metadata
        final Table unqualifiedTable = new Table(null, null, "test_person");
        final Row row = new Row(List.of(
                new RowColumn("id", 1L, new Column(unqualifiedTable, "id")),
                new RowColumn("name", "Alice", new Column(unqualifiedTable, "name"))
        ));

        // When
        final List<TestPersonDto> result = dtoMapper.toDtos(TestPersonDto.class, null, List.of(row));

        // Then
        assertEquals(1, result.size());
        final TestPersonDto person = result.getFirst();
        assertEquals(1L, person.id);
        assertEquals("Alice", person.name);
    }

    @Test
    void toDtos_plainColumnNameWithoutTable() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table canonicalTable = new Table("", "public", "test_person");
        final ColumnMetaData idCol = new ColumnMetaData(canonicalTable, "id", false, Types.BIGINT);
        final ColumnMetaData nameCol = new ColumnMetaData(canonicalTable, "name", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(canonicalTable, List.of("id"), List.of(idCol, nameCol));
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);
        final FieldAccessor idField = cache.fieldAccessorOrThrow(TestPersonDto.class, "id");
        final FieldAccessor nameField = cache.fieldAccessorOrThrow(TestPersonDto.class, "name");
        final OrmTable ormTable = new OrmTable(
                TestPersonDto.class,
                metaData,
                Map.of(idField, idCol, nameField, nameCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(TestPersonDto.class, ormTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);

        // RowColumn has null column or Column with no table as returned by Oracle JDBC metadata
        final Row row = new Row(List.of(
                new RowColumn("id", 2L, null),
                new RowColumn("name", "Bob", new Column(null, "name"))
        ));

        // When
        final List<TestPersonDto> result = dtoMapper.toDtos(TestPersonDto.class, null, List.of(row));

        // Then
        assertEquals(1, result.size());
        final TestPersonDto person = result.getFirst();
        assertEquals(2L, person.id);
        assertEquals("Bob", person.name);
    }

    @Test
    void toDtos_unregisteredDtoThrows() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);
        final Row row = new Row(List.of(
                new RowColumn("id", 1L, null)
        ));

        // When / Then
        assertThrows(NullPointerException.class, () -> dtoMapper.toDtos(TestPersonDto.class, null, List.of(row)));
    }

    @Test
    void toDtos_unknownColumnLabelThrows() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table canonicalTable = new Table("", "public", "test_person");
        final ColumnMetaData idCol = new ColumnMetaData(canonicalTable, "id", false, Types.BIGINT);
        final TableMetaData metaData = new TableMetaData(canonicalTable, List.of("id"), List.of(idCol));
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);
        final FieldAccessor idField = cache.fieldAccessorOrThrow(TestPersonDto.class, "id");
        final OrmTable ormTable = new OrmTable(
                TestPersonDto.class,
                metaData,
                Map.of(idField, idCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(TestPersonDto.class, ormTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);

        final Row row = new Row(List.of(
                new RowColumn("non_existent_column", "value", null)
        ));

        // When / Then
        assertThrows(IllegalStateException.class, () -> dtoMapper.toDtos(TestPersonDto.class, null, List.of(row)));
    }

    @Test
    void toDtos_cachingMappingPlan_reusesCachedPlan() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table canonicalTable = new Table("", "public", "test_person");
        final ColumnMetaData idCol = new ColumnMetaData(canonicalTable, "id", false, Types.BIGINT);
        final ColumnMetaData nameCol = new ColumnMetaData(canonicalTable, "name", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(canonicalTable, List.of("id"), List.of(idCol, nameCol));
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);
        final FieldAccessor idField = cache.fieldAccessorOrThrow(TestPersonDto.class, "id");
        final FieldAccessor nameField = cache.fieldAccessorOrThrow(TestPersonDto.class, "name");
        final OrmTable ormTable = new OrmTable(
                TestPersonDto.class,
                metaData,
                Map.of(idField, idCol, nameField, nameCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(TestPersonDto.class, ormTable);

        final MappingPlanCache mappingPlanCache = new MappingPlanCache();
        final DtoMapper dtoMapper = createDtoMapperWithCache(tableRegistry, mappingPlanCache);

        final Row row1 = new Row(List.of(
                new RowColumn("id", 1L, new Column(canonicalTable, "id")),
                new RowColumn("name", "Alice", new Column(canonicalTable, "name"))
        ));
        final Row row2 = new Row(List.of(
                new RowColumn("id", 2L, new Column(canonicalTable, "id")),
                new RowColumn("name", "Bob", new Column(canonicalTable, "name"))
        ));

        // When
        assertEquals(0, mappingPlanCache.size());
        final List<TestPersonDto> result1 = dtoMapper.toDtos(TestPersonDto.class, null, List.of(row1));
        assertEquals(1, mappingPlanCache.size());

        final List<TestPersonDto> result2 = dtoMapper.toDtos(TestPersonDto.class, null, List.of(row2));

        // Then
        assertEquals(1, mappingPlanCache.size());
        assertEquals("Alice", result1.getFirst().name);
        assertEquals("Bob", result2.getFirst().name);
    }

    @Test
    void toDtos_recordCanonicalConstructor() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table table = new Table("person_record");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", false, Types.BIGINT);
        final ColumnMetaData nameCol = new ColumnMetaData(table, "name", false, Types.VARCHAR);
        final ColumnMetaData ageCol = new ColumnMetaData(table, "age", false, Types.INTEGER);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol, nameCol, ageCol));
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);
        final FieldAccessor idField = cache.fieldAccessorOrThrow(PersonRecord.class, "id");
        final FieldAccessor nameField = cache.fieldAccessorOrThrow(PersonRecord.class, "name");
        final FieldAccessor ageField = cache.fieldAccessorOrThrow(PersonRecord.class, "age");
        final OrmTable ormTable = new OrmTable(
                PersonRecord.class,
                metaData,
                Map.of(idField, idCol, nameField, nameCol, ageField, ageCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(PersonRecord.class, ormTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);
        final Row row = new Row(List.of(
                new RowColumn("id", 10L, new Column(table, "id")),
                new RowColumn("name", "Charlie", new Column(table, "name")),
                new RowColumn("age", 30, new Column(table, "age"))
        ));

        // When
        final List<PersonRecord> result = dtoMapper.toDtos(PersonRecord.class, null, List.of(row));

        // Then
        assertEquals(1, result.size());
        final PersonRecord person = result.getFirst();
        assertEquals(10L, person.id());
        assertEquals("Charlie", person.name());
        assertEquals(30, person.age());
    }

    @Test
    void toDtos_compositePrimaryKey() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table table = new Table("composite_entity");
        final ColumnMetaData tenantIdCol = new ColumnMetaData(table, "tenant_id", false, Types.VARCHAR);
        final ColumnMetaData entityIdCol = new ColumnMetaData(table, "entity_id", false, Types.BIGINT);
        final ColumnMetaData valCol = new ColumnMetaData(table, "val", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(table, List.of("tenant_id", "entity_id"), List.of(tenantIdCol, entityIdCol, valCol));
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);
        final FieldAccessor tenantIdField = cache.fieldAccessorOrThrow(CompositeKeyDto.class, "tenantId");
        final FieldAccessor entityIdField = cache.fieldAccessorOrThrow(CompositeKeyDto.class, "entityId");
        final FieldAccessor valField = cache.fieldAccessorOrThrow(CompositeKeyDto.class, "val");

        final OrmTable ormTable = new OrmTable(
                CompositeKeyDto.class,
                metaData,
                Map.of(tenantIdField, tenantIdCol, entityIdField, entityIdCol, valField, valCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(CompositeKeyDto.class, ormTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);
        final Row row1 = new Row(List.of(
                new RowColumn("tenant_id", "tenantA", new Column(table, "tenant_id")),
                new RowColumn("entity_id", 100L, new Column(table, "entity_id")),
                new RowColumn("val", "First", new Column(table, "val"))
        ));
        final Row row2 = new Row(List.of(
                new RowColumn("tenant_id", "tenantB", new Column(table, "tenant_id")),
                new RowColumn("entity_id", 100L, new Column(table, "entity_id")),
                new RowColumn("val", "Second", new Column(table, "val"))
        ));

        // When
        final List<CompositeKeyDto> result = dtoMapper.toDtos(CompositeKeyDto.class, null, List.of(row1, row2));

        // Then
        assertEquals(2, result.size());
        assertEquals("tenantA", result.get(0).tenantId);
        assertEquals("tenantB", result.get(1).tenantId);
    }

    @Test
    void toDtos_nullAndDefaultValues() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table table = new Table("person_record");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", false, Types.BIGINT);
        final ColumnMetaData nameCol = new ColumnMetaData(table, "name", false, Types.VARCHAR);
        final ColumnMetaData ageCol = new ColumnMetaData(table, "age", false, Types.INTEGER);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol, nameCol, ageCol));
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);
        final FieldAccessor idField = cache.fieldAccessorOrThrow(PersonRecord.class, "id");
        final FieldAccessor nameField = cache.fieldAccessorOrThrow(PersonRecord.class, "name");
        final FieldAccessor ageField = cache.fieldAccessorOrThrow(PersonRecord.class, "age");
        final OrmTable ormTable = new OrmTable(
                PersonRecord.class,
                metaData,
                Map.of(idField, idCol, nameField, nameCol, ageField, ageCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(PersonRecord.class, ormTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);
        // Only id is provided, name and age omitted in row
        final Row row = new Row(List.of(
                new RowColumn("id", 10L, new Column(table, "id"))
        ));

        // When
        final List<PersonRecord> result = dtoMapper.toDtos(PersonRecord.class, null, List.of(row));

        // Then
        assertEquals(1, result.size());
        final PersonRecord person = result.getFirst();
        assertEquals(10L, person.id());
        assertNull(person.name());
        assertEquals(0, person.age());
    }

    @Test
    void toDtos_deduplicationByIdentity() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table table = new Table("test_person");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", false, Types.BIGINT);
        final ColumnMetaData nameCol = new ColumnMetaData(table, "name", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol, nameCol));
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);
        final FieldAccessor idField = cache.fieldAccessorOrThrow(TestPersonDto.class, "id");
        final FieldAccessor nameField = cache.fieldAccessorOrThrow(TestPersonDto.class, "name");
        final OrmTable ormTable = new OrmTable(
                TestPersonDto.class,
                metaData,
                Map.of(idField, idCol, nameField, nameCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(TestPersonDto.class, ormTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);
        final Row row1 = new Row(List.of(
                new RowColumn("id", 1L, new Column(table, "id")),
                new RowColumn("name", "Alice", new Column(table, "name"))
        ));
        final Row row2 = new Row(List.of(
                new RowColumn("id", 1L, new Column(table, "id")),
                new RowColumn("name", "Alice", new Column(table, "name"))
        ));

        // When
        final List<TestPersonDto> result = dtoMapper.toDtos(TestPersonDto.class, null, List.of(row1, row2));

        // Then
        assertEquals(1, result.size());
    }

    @Test
    void toDtos_sqlFunctionColumnMatching() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table canonicalTable = new Table("", "public", "test_person");
        final ColumnMetaData idCol = new ColumnMetaData(canonicalTable, "id", false, Types.BIGINT);
        final ColumnMetaData nameCol = new ColumnMetaData(canonicalTable, "name", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(canonicalTable, List.of("id"), List.of(idCol, nameCol));
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);
        final FieldAccessor idField = cache.fieldAccessorOrThrow(TestPersonDto.class, "id");
        final FieldAccessor nameField = cache.fieldAccessorOrThrow(TestPersonDto.class, "name");
        final OrmTable ormTable = new OrmTable(
                TestPersonDto.class,
                metaData,
                Map.of(idField, idCol, nameField, nameCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(TestPersonDto.class, ormTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);
        final Row row = new Row(List.of(
                new RowColumn("MAX(test_person.id)", 5L, null),
                new RowColumn("LOWER(test_person.name)", "david", null)
        ));

        // When
        final List<TestPersonDto> result = dtoMapper.toDtos(TestPersonDto.class, null, List.of(row));

        // Then
        assertEquals(1, result.size());
        assertEquals(5L, result.getFirst().id);
        assertEquals("david", result.getFirst().name);
    }

    @Test
    void toDtos_partialObjectIfNoJoin() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table orderTable = new Table("orders");
        final Table userTable = new Table("users");

        final ColumnMetaData orderIdCol = new ColumnMetaData(orderTable, "id", false, Types.BIGINT);
        final ColumnMetaData orderUserIdCol = new ColumnMetaData(orderTable, "user_id", false, Types.BIGINT);
        final TableMetaData orderMeta = new TableMetaData(orderTable, List.of("id"), List.of(orderIdCol, orderUserIdCol));

        final ColumnMetaData userIdCol = new ColumnMetaData(userTable, "id", false, Types.BIGINT);
        final ColumnMetaData userNameCol = new ColumnMetaData(userTable, "name", false, Types.VARCHAR);
        final TableMetaData userMeta = new TableMetaData(userTable, List.of("id"), List.of(userIdCol, userNameCol));

        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);

        final FieldAccessor orderIdField = cache.fieldAccessorOrThrow(OrderDto.class, "id");
        final FieldAccessor orderUserField = cache.fieldAccessorOrThrow(OrderDto.class, "user");

        final FieldAccessor userIdField = cache.fieldAccessorOrThrow(TestPersonDto.class, "id");
        final FieldAccessor userNameField = cache.fieldAccessorOrThrow(TestPersonDto.class, "name");

        final OrmTable userOrmTable = new OrmTable(
                TestPersonDto.class,
                userMeta,
                Map.of(userIdField, userIdCol, userNameField, userNameCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(TestPersonDto.class, userOrmTable);

        final ColumnAndInlineTable columnAndInlineTable = new ColumnAndInlineTable(orderUserIdCol, userOrmTable);
        final OrmTable orderOrmTable = new OrmTable(
                OrderDto.class,
                orderMeta,
                Map.of(orderIdField, orderIdCol, orderUserField, columnAndInlineTable),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(OrderDto.class, orderOrmTable);

        final LitebridgeContext context = mock(LitebridgeContext.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        when(typeConverter.convert(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.typeConverter()).thenReturn(typeConverter);
        when(context.getRelatedDtoStrategy()).thenReturn(RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DtoMapper dtoMapper = new DtoMapper(dtoConstructor, context);

        final Row row = new Row(List.of(
                new RowColumn("id", 100L, new Column(orderTable, "id")),
                new RowColumn("user_id", 42L, new Column(orderTable, "user_id"))
        ));

        // When
        final List<OrderDto> result = dtoMapper.toDtos(OrderDto.class, null, List.of(row));

        // Then
        assertEquals(1, result.size());
        assertEquals(100L, result.getFirst().id);
        assertNotNull(result.getFirst().user);
        assertEquals(42L, result.getFirst().user.id);
        assertNull(result.getFirst().user.name);
    }

    @Test
    void toDtos_oneToManyRelationship() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table departmentTable = new Table("department");
        final Table employeeTable = new Table("employee");

        final ColumnMetaData deptIdCol = new ColumnMetaData(departmentTable, "id", false, Types.BIGINT);
        final ColumnMetaData deptNameCol = new ColumnMetaData(departmentTable, "name", false, Types.VARCHAR);
        final TableMetaData deptMeta = new TableMetaData(departmentTable, List.of("id"), List.of(deptIdCol, deptNameCol));

        final ColumnMetaData empIdCol = new ColumnMetaData(employeeTable, "id", false, Types.BIGINT);
        final ColumnMetaData empNameCol = new ColumnMetaData(employeeTable, "name", false, Types.VARCHAR);
        final ColumnMetaData empDeptIdCol = new ColumnMetaData(employeeTable, "dept_id", false, Types.BIGINT);
        final TableMetaData empMeta = new TableMetaData(employeeTable, List.of("id"), List.of(empIdCol, empNameCol, empDeptIdCol));

        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);

        final FieldAccessor deptIdField = cache.fieldAccessorOrThrow(DepartmentDto.class, "id");
        final FieldAccessor deptNameField = cache.fieldAccessorOrThrow(DepartmentDto.class, "name");
        final FieldAccessor deptEmployeesField = cache.fieldAccessorOrThrow(DepartmentDto.class, "employees");

        final FieldAccessor empIdField = cache.fieldAccessorOrThrow(EmployeeDto.class, "id");
        final FieldAccessor empNameField = cache.fieldAccessorOrThrow(EmployeeDto.class, "name");
        final FieldAccessor empDeptIdField = cache.fieldAccessorOrThrow(EmployeeDto.class, "deptId");

        final MappedOneToMany mappedOneToMany = new MappedOneToMany(empDeptIdField, deptEmployeesField);

        final Map<FieldAccessor, MappedFieldTarget> deptTargetMap = new LinkedHashMap<>();
        deptTargetMap.put(deptIdField, deptIdCol);
        deptTargetMap.put(deptNameField, deptNameCol);
        deptTargetMap.put(deptEmployeesField, mappedOneToMany);

        final OrmTable deptOrmTable = new OrmTable(
                DepartmentDto.class,
                deptMeta,
                deptTargetMap,
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(DepartmentDto.class, deptOrmTable);

        final OrmTable empOrmTable = new OrmTable(
                EmployeeDto.class,
                empMeta,
                Map.of(empIdField, empIdCol, empNameField, empNameCol, empDeptIdField, empDeptIdCol),
                new ChangeTracker(lookup),
                cache);
        empOrmTable.addOneToManyReverseMapping(deptEmployeesField);
        tableRegistry.addTable(EmployeeDto.class, empOrmTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);

        final Row row1 = new Row(List.of(
                new RowColumn("id", 1L, new Column(departmentTable, "id"), "d"),
                new RowColumn("name", "Engineering", new Column(departmentTable, "name"), "d"),
                new RowColumn("emp_id", 101L, new Column(employeeTable, "id"), "e"),
                new RowColumn("emp_name", "Alice", new Column(employeeTable, "name"), "e"),
                new RowColumn("dept_id", 1L, new Column(employeeTable, "dept_id"), "e")
        ));
        final Row row2 = new Row(List.of(
                new RowColumn("id", 1L, new Column(departmentTable, "id"), "d"),
                new RowColumn("name", "Engineering", new Column(departmentTable, "name"), "d"),
                new RowColumn("emp_id", 102L, new Column(employeeTable, "id"), "e"),
                new RowColumn("emp_name", "Bob", new Column(employeeTable, "name"), "e"),
                new RowColumn("dept_id", 1L, new Column(employeeTable, "dept_id"), "e")
        ));

        // When
        final List<DepartmentDto> result = dtoMapper.toDtos(DepartmentDto.class, null, List.of(row1, row2));

        // Then
        assertEquals(1, result.size());
        final DepartmentDto dept = result.getFirst();
        assertEquals(1L, dept.id);
        assertEquals("Engineering", dept.name);
        assertNotNull(dept.employees);
        assertEquals(2, dept.employees.size());
    }

    @Test
    void constructDto_staticMethodRecordAndPojo() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);

        final FieldAccessor idField = cache.fieldAccessorOrThrow(TestPersonDto.class, "id");
        final FieldAccessor nameField = cache.fieldAccessorOrThrow(TestPersonDto.class, "name");

        final Table table = new Table("test_person");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", false, Types.BIGINT);
        final ColumnMetaData nameCol = new ColumnMetaData(table, "name", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol, nameCol));
        final OrmTable ormTable = new OrmTable(
                TestPersonDto.class,
                metaData,
                Map.of(idField, idCol, nameField, nameCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(TestPersonDto.class, ormTable);

        // When
        final TestPersonDto pojo = DtoMapper.constructDto(TestPersonDto.class, List.of(
                new DtoConstructor.FieldAccessorValue(idField, 99L),
                new DtoConstructor.FieldAccessorValue(nameField, "Constructed")
        ), dtoConstructor);

        // Then
        assertEquals(99L, pojo.id);
        assertEquals("Constructed", pojo.name);
    }

    @Test
    void toDtos_manyToManyRelationship() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table studentTable = new Table("student");
        final Table courseTable = new Table("course");
        final Table joinTable = new Table("student_course");

        final ColumnMetaData studentIdCol = new ColumnMetaData(studentTable, "id", false, Types.BIGINT);
        final ColumnMetaData studentNameCol = new ColumnMetaData(studentTable, "name", false, Types.VARCHAR);
        final TableMetaData studentMeta = new TableMetaData(studentTable, List.of("id"), List.of(studentIdCol, studentNameCol));

        final ColumnMetaData courseIdCol = new ColumnMetaData(courseTable, "id", false, Types.BIGINT);
        final ColumnMetaData courseTitleCol = new ColumnMetaData(courseTable, "title", false, Types.VARCHAR);
        final TableMetaData courseMeta = new TableMetaData(courseTable, List.of("id"), List.of(courseIdCol, courseTitleCol));

        final ColumnMetaData joinStudentIdCol = new ColumnMetaData(joinTable, "student_id", false, Types.BIGINT);
        final ColumnMetaData joinCourseIdCol = new ColumnMetaData(joinTable, "course_id", false, Types.BIGINT);
        final TableMetaData joinMeta = new TableMetaData(joinTable, List.of("student_id", "course_id"), List.of(joinStudentIdCol, joinCourseIdCol));

        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);

        final FieldAccessor studentIdField = cache.fieldAccessorOrThrow(StudentDto.class, "id");
        final FieldAccessor studentNameField = cache.fieldAccessorOrThrow(StudentDto.class, "name");
        final FieldAccessor studentCoursesField = cache.fieldAccessorOrThrow(StudentDto.class, "courses");

        final FieldAccessor courseIdField = cache.fieldAccessorOrThrow(CourseDto.class, "id");
        final FieldAccessor courseTitleField = cache.fieldAccessorOrThrow(CourseDto.class, "title");

        final OrmTable courseOrmTable = new OrmTable(
                CourseDto.class,
                courseMeta,
                Map.of(courseIdField, courseIdCol, courseTitleField, courseTitleCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(CourseDto.class, courseOrmTable);

        final OrmTable joinOrmTable = new OrmTable(
                StudentCourseDto.class,
                joinMeta,
                Map.of(),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(StudentCourseDto.class, joinOrmTable);

        final MappedManyToMany mappedManyToMany = new MappedManyToMany(
                joinOrmTable,
                new String[]{"student_id"},
                studentCoursesField,
                () -> courseOrmTable,
                new String[]{"course_id"});

        final Map<FieldAccessor, MappedFieldTarget> studentTargets = new LinkedHashMap<>();
        studentTargets.put(studentIdField, studentIdCol);
        studentTargets.put(studentNameField, studentNameCol);
        studentTargets.put(studentCoursesField, mappedManyToMany);

        final OrmTable studentOrmTable = new OrmTable(
                StudentDto.class,
                studentMeta,
                studentTargets,
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(StudentDto.class, studentOrmTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);

        final Row row1 = new Row(List.of(
                new RowColumn("s_id", 1L, new Column(studentTable, "id"), "s"),
                new RowColumn("s_name", "Student1", new Column(studentTable, "name"), "s"),
                new RowColumn("c_id", 10L, new Column(courseTable, "id"), "c"),
                new RowColumn("c_title", "Math", new Column(courseTable, "title"), "c")
        ));
        final Row row2 = new Row(List.of(
                new RowColumn("s_id", 1L, new Column(studentTable, "id"), "s"),
                new RowColumn("s_name", "Student1", new Column(studentTable, "name"), "s"),
                new RowColumn("c_id", 20L, new Column(courseTable, "id"), "c"),
                new RowColumn("c_title", "Physics", new Column(courseTable, "title"), "c")
        ));

        // When
        final List<StudentDto> result = dtoMapper.toDtos(StudentDto.class, null, List.of(row1, row2));

        // Then
        assertEquals(1, result.size());
        final StudentDto student = result.getFirst();
        assertEquals(1L, student.id);
        assertEquals("Student1", student.name);
        assertNotNull(student.courses);
        assertEquals(2, student.courses.size());
    }

    @Test
    void toDtos_withContextDtoClass() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table deptTable = new Table("dept");
        final ColumnMetaData deptIdCol = new ColumnMetaData(deptTable, "id", false, Types.BIGINT);
        final ColumnMetaData deptNameCol = new ColumnMetaData(deptTable, "name", false, Types.VARCHAR);
        final TableMetaData deptMetaData = new TableMetaData(deptTable, List.of("id"), List.of(deptIdCol, deptNameCol));

        final Table personTable = new Table("context_person");
        final ColumnMetaData personIdCol = new ColumnMetaData(personTable, "id", false, Types.BIGINT);
        final ColumnMetaData personNameCol = new ColumnMetaData(personTable, "name", false, Types.VARCHAR);
        final TableMetaData personMetaData = new TableMetaData(personTable, List.of("id"), List.of(personIdCol, personNameCol));

        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);

        final FieldAccessor deptIdField = cache.fieldAccessorOrThrow(DepartmentDto.class, "id");
        final FieldAccessor deptNameField = cache.fieldAccessorOrThrow(DepartmentDto.class, "name");
        final OrmTable deptOrmTable = new OrmTable(
                DepartmentDto.class,
                deptMetaData,
                Map.of(deptIdField, deptIdCol, deptNameField, deptNameCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(DepartmentDto.class, deptOrmTable);

        final FieldAccessor personIdField = cache.fieldAccessorOrThrow(TestPersonDto.class, "id");
        final FieldAccessor personNameField = cache.fieldAccessorOrThrow(TestPersonDto.class, "name");
        final OrmTable personOrmTable = new OrmTable(
                TestPersonDto.class,
                personMetaData,
                Map.of(personIdField, personIdCol, personNameField, personNameCol),
                new ChangeTracker(lookup),
                cache);
        deptOrmTable.getContextTableRegistry().addTable(TestPersonDto.class, personOrmTable);
        tableRegistry.addTable(DepartmentDto.class, deptOrmTable);
        tableRegistry.addTable(personOrmTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);
        final Row row = new Row(List.of(
                new RowColumn("id", 1L, new Column(personTable, "id")),
                new RowColumn("name", "Alice", new Column(personTable, "name"))
        ));

        // When
        final List<TestPersonDto> result = dtoMapper.toDtos(TestPersonDto.class, DepartmentDto.class, List.of(row));

        // Then
        assertEquals(1, result.size());
        assertEquals("Alice", result.getFirst().name);
    }

    @Test
    void toDtos_relatedDtoWithoutJoinReturnsNull() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table orderTable = new Table("orders");
        final Table userTable = new Table("users");

        final ColumnMetaData orderIdCol = new ColumnMetaData(orderTable, "id", false, Types.BIGINT);
        final ColumnMetaData orderUserIdCol = new ColumnMetaData(orderTable, "user_id", false, Types.BIGINT);
        final TableMetaData orderMeta = new TableMetaData(orderTable, List.of("id"), List.of(orderIdCol, orderUserIdCol));

        final ColumnMetaData userIdCol = new ColumnMetaData(userTable, "id", false, Types.BIGINT);
        final ColumnMetaData userNameCol = new ColumnMetaData(userTable, "name", false, Types.VARCHAR);
        final TableMetaData userMeta = new TableMetaData(userTable, List.of("id"), List.of(userIdCol, userNameCol));

        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);

        final FieldAccessor orderIdField = cache.fieldAccessorOrThrow(OrderDto.class, "id");
        final FieldAccessor orderUserField = cache.fieldAccessorOrThrow(OrderDto.class, "user");

        final FieldAccessor userIdField = cache.fieldAccessorOrThrow(TestPersonDto.class, "id");
        final FieldAccessor userNameField = cache.fieldAccessorOrThrow(TestPersonDto.class, "name");

        final OrmTable userOrmTable = new OrmTable(
                TestPersonDto.class,
                userMeta,
                Map.of(userIdField, userIdCol, userNameField, userNameCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(TestPersonDto.class, userOrmTable);

        final ColumnAndInlineTable columnAndInlineTable = new ColumnAndInlineTable(orderUserIdCol, userOrmTable);
        final OrmTable orderOrmTable = new OrmTable(
                OrderDto.class,
                orderMeta,
                Map.of(orderIdField, orderIdCol, orderUserField, columnAndInlineTable),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(OrderDto.class, orderOrmTable);

        final LitebridgeContext context = mock(LitebridgeContext.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        when(typeConverter.convert(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.typeConverter()).thenReturn(typeConverter);
        when(context.getRelatedDtoStrategy()).thenReturn(null);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final DtoMapper dtoMapper = new DtoMapper(dtoConstructor, context);

        final Row row = new Row(List.of(
                new RowColumn("id", 100L, new Column(orderTable, "id")),
                new RowColumn("user_id", 42L, new Column(orderTable, "user_id"))
        ));

        // When
        final List<OrderDto> result = dtoMapper.toDtos(OrderDto.class, null, List.of(row));

        // Then
        assertEquals(1, result.size());
        assertEquals(100L, result.getFirst().id);
        assertNull(result.getFirst().user);
    }

    @Test
    void toDtos_compositeForeignKeyRelatedDto() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final Table parentTable = new Table("parent_comp");
        final Table childTable = new Table("child_comp");

        final ColumnMetaData parentIdCol = new ColumnMetaData(parentTable, "id", false, Types.BIGINT);
        final ColumnMetaData parentTenantCol = new ColumnMetaData(parentTable, "tenant_id", false, Types.VARCHAR);
        final ColumnMetaData parentChildTenantCol = new ColumnMetaData(parentTable, "child_tenant_id", false, Types.VARCHAR);
        final ColumnMetaData parentChildEntityCol = new ColumnMetaData(parentTable, "child_entity_id", false, Types.BIGINT);
        final TableMetaData parentMeta = new TableMetaData(parentTable, List.of("id"), List.of(parentIdCol, parentTenantCol, parentChildTenantCol, parentChildEntityCol));

        final ColumnMetaData childTenantCol = new ColumnMetaData(childTable, "tenant_id", false, Types.VARCHAR);
        final ColumnMetaData childEntityCol = new ColumnMetaData(childTable, "entity_id", false, Types.BIGINT);
        final ColumnMetaData childValCol = new ColumnMetaData(childTable, "val", false, Types.VARCHAR);
        final TableMetaData childMeta = new TableMetaData(childTable, List.of("tenant_id", "entity_id"), List.of(childTenantCol, childEntityCol, childValCol));

        parentChildTenantCol.setJoinColumnSupplier(() -> childTenantCol);
        parentChildEntityCol.setJoinColumnSupplier(() -> childEntityCol);

        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(lookup);

        final FieldAccessor childTenantField = cache.fieldAccessorOrThrow(CompositeKeyDto.class, "tenantId");
        final FieldAccessor childEntityField = cache.fieldAccessorOrThrow(CompositeKeyDto.class, "entityId");
        final FieldAccessor childValField = cache.fieldAccessorOrThrow(CompositeKeyDto.class, "val");

        final OrmTable childOrmTable = new OrmTable(
                CompositeKeyDto.class,
                childMeta,
                Map.of(childTenantField, childTenantCol, childEntityField, childEntityCol, childValField, childValCol),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(CompositeKeyDto.class, childOrmTable);

        final FieldAccessor parentIdField = cache.fieldAccessorOrThrow(ParentWithCompositeChildDto.class, "id");
        final FieldAccessor parentChildField = cache.fieldAccessorOrThrow(ParentWithCompositeChildDto.class, "child");

        final MappedCompositeKey compositeKey = new MappedCompositeKey(
                new MappedFieldTarget[]{parentChildTenantCol, parentChildEntityCol},
                () -> childOrmTable,
                new String[]{"tenant_id", "entity_id"}
        );
        final OrmTable parentOrmTable = new OrmTable(
                ParentWithCompositeChildDto.class,
                parentMeta,
                Map.of(parentIdField, parentIdCol, parentChildField, compositeKey),
                new ChangeTracker(lookup),
                cache);
        tableRegistry.addTable(ParentWithCompositeChildDto.class, parentOrmTable);

        final DtoMapper dtoMapper = createDtoMapper(tableRegistry);
        final Row row = new Row(List.of(
                new RowColumn("id", 1L, new Column(parentTable, "id"), "p"),
                new RowColumn("child_tenant_id", "tenantX", new Column(parentTable, "child_tenant_id"), "p"),
                new RowColumn("child_entity_id", 999L, new Column(parentTable, "child_entity_id"), "p"),
                new RowColumn("tenant_id", "tenantX", new Column(childTable, "tenant_id"), "c"),
                new RowColumn("entity_id", 999L, new Column(childTable, "entity_id"), "c"),
                new RowColumn("val", "CompositeVal", new Column(childTable, "val"), "c")
        ));

        // When
        final List<ParentWithCompositeChildDto> result = dtoMapper.toDtos(ParentWithCompositeChildDto.class, null, List.of(row));

        // Then
        assertEquals(1, result.size());
        final ParentWithCompositeChildDto parent = result.getFirst();
        assertEquals(1L, parent.id);
        assertNotNull(parent.child);
        assertEquals("tenantX", parent.child.tenantId);
        assertEquals(999L, parent.child.entityId);
        assertEquals("CompositeVal", parent.child.val);
    }

    private static DtoMapper createDtoMapper(final TableRegistry tableRegistry) {
        return createDtoMapperWithCache(tableRegistry, null);
    }

    private static DtoMapper createDtoMapperWithCache(final TableRegistry tableRegistry, final MappingPlanCache cache) {
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        when(typeConverter.convert(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.typeConverter()).thenReturn(typeConverter);
        when(context.mappingPlanCache()).thenReturn(cache);

        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        return new DtoMapper(dtoConstructor, context);
    }

    public static class TestPersonDto {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

    public record PersonRecord(long id, String name, int age) {
    }

    public static class CompositeKeyDto {
        private String tenantId;
        private Long entityId;
        private String val;

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(final String tenantId) {
            this.tenantId = tenantId;
        }

        public Long getEntityId() {
            return entityId;
        }

        public void setEntityId(final Long entityId) {
            this.entityId = entityId;
        }

        public String getVal() {
            return val;
        }

        public void setVal(final String val) {
            this.val = val;
        }
    }

    public static class LogEntryDto {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(final String message) {
            this.message = message;
        }
    }

    public static class OrderDto {
        private Long id;
        private TestPersonDto user;

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public TestPersonDto getUser() {
            return user;
        }

        public void setUser(final TestPersonDto user) {
            this.user = user;
        }
    }

    public static class DepartmentDto {
        private Long id;
        private String name;
        private List<EmployeeDto> employees = new ArrayList<>();

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public List<EmployeeDto> getEmployees() {
            return employees;
        }

        public void setEmployees(final List<EmployeeDto> employees) {
            this.employees = employees;
        }
    }

    public static class EmployeeDto {
        private Long id;
        private String name;
        private Long deptId;

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public Long getDeptId() {
            return deptId;
        }

        public void setDeptId(final Long deptId) {
            this.deptId = deptId;
        }
    }

    public static class StudentDto {
        private Long id;
        private String name;
        private List<CourseDto> courses = new ArrayList<>();

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public List<CourseDto> getCourses() {
            return courses;
        }

        public void setCourses(final List<CourseDto> courses) {
            this.courses = courses;
        }
    }

    public static class CourseDto {
        private Long id;
        private String title;

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(final String title) {
            this.title = title;
        }
    }

    public static class StudentCourseDto {
        private Long studentId;
        private Long courseId;

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(final Long studentId) {
            this.studentId = studentId;
        }

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(final Long courseId) {
            this.courseId = courseId;
        }
    }

    public static class ParentWithCompositeChildDto {
        private Long id;
        private CompositeKeyDto child;

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public CompositeKeyDto getChild() {
            return child;
        }

        public void setChild(final CompositeKeyDto child) {
            this.child = child;
        }
    }
}
