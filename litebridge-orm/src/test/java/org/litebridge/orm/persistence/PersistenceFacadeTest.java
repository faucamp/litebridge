package org.litebridge.orm.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.impl.expression.SqlFunctionRegistryFactory;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.engine.SelectEngine;
import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;
import org.litebridge.orm.persistence.manytomany.HiddenJoinEntity;
import org.litebridge.orm.persistence.manytomany.NoOpFieldAccessor;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistenceFacadeTest {

    private static final LabelGenerator labelGenerator = new LabelGenerator();

    private final Map<String, TableMetaData> metaDataMap = new HashMap<>();
    @Mock
    private TableRegistry tableRegistry;
    @Mock
    private TransactionManager transactionManager;
    @Mock
    private TransactionalDatabaseProvider databaseProvider;
    private ChangeTracker changeTracker;
    private DtoConstructor dtoConstructor;
    private PersistenceFacade persistenceFacade;

    @BeforeEach
    void beforeEach() {
        changeTracker = new ChangeTracker(MethodHandles.lookup());
        dtoConstructor = new DtoConstructor(tableRegistry);
        persistenceFacade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);
    }

    @Test
    void insert() throws SQLException {
        // Given
        final CustomerDto dto = new CustomerDto();
        dto.name = "test";
        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        lenient().when(databaseProvider.typeConverter()).thenReturn(new DefaultTypeConverter());
        lenient().when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1));
        lenient().when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1));

        // When
        persistenceFacade.insert(dto);

        // Then
        verify(databaseProvider).executeUpdate(any(), eq(InsertResult.class), any());
    }

    @Test
    void update() throws SQLException {
        // Given
        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "new name";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        table.trackDto(dto);
        // Simulate change
        dto.name = "changed";

        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.executeUpdate(any(), eq(UpdateResult.class), any())).thenReturn(new UpdateResult(1));

        // When
        persistenceFacade.update(dto);

        // Then
        verify(databaseProvider).executeUpdate(any(), eq(UpdateResult.class), any());
    }

    @Test
    void delete() throws SQLException {
        // Given
        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.executeUpdate(any(), eq(UpdateResult.class), any())).thenReturn(new UpdateResult(1));

        // When
        persistenceFacade.delete(dto);

        // Then
        verify(databaseProvider).executeUpdate(any(), eq(UpdateResult.class), any());
    }

    @Test
    void save_collection() throws SQLException {
        // Given
        final CustomerDto c1 = new CustomerDto();
        c1.name = "c1";
        final CustomerDto c2 = new CustomerDto();
        c2.name = "c2";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1));

        // When
        persistenceFacade.save(List.of(c1, c2));

        // Then
        verify(databaseProvider, times(2)).executeUpdate(any(), eq(InsertResult.class), any());
    }

    @Test
    void save_cycle_doesNotThrow() throws SQLException {
        // Given
        final ProductDto p1 = new ProductDto();
        p1.name = "p1";
        final ProductDto p2 = new ProductDto();
        p2.name = "p2";
        // Circular dependency
        p1.relatedProduct = p2;
        p2.relatedProduct = p1;

        final OrmTable table = createOrmTable(changeTracker, ProductDto.class, "products", Map.of("id", numeric("ID"), "name", varchar("NAME"), "relatedProduct", numeric("RELATED_ID")), List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(ProductDto.class)).thenReturn(table);
        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1));

        // When
        persistenceFacade.save(p1);

        // Then
        verify(databaseProvider, times(2)).executeUpdate(any(), eq(InsertResult.class), any());
    }

    @Test
    void save_record() throws SQLException {
        // Given
        final PersonRecord person = new PersonRecord(null, "John");

        final OrmTable table = createOrmTable(changeTracker, PersonRecord.class, "persons", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(PersonRecord.class)).thenReturn(table);
        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1, Map.of(table.getMetaData().column("ID"), 123L)));

        // When
        persistenceFacade.save(person);

        // Then
        verify(databaseProvider).executeUpdate(any(), eq(InsertResult.class), any());
    }

    @Test
    void save_noChanges() throws SQLException {
        // Given
        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "test";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        table.syncPersistedDto(dto); // Marks as persisted and takes snapshot

        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);

        // When
        persistenceFacade.save(dto);

        // Then
        verify(databaseProvider, never()).executeUpdate(any(), eq(UpdateResult.class), any());
    }

    @Test
    void updateDtoPrimaryKey_rollback() throws SQLException {
        // Given
        final CustomerDto dto = new CustomerDto();
        dto.name = "test";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1, Map.of(table.getMetaData().column("ID"), 1L)));

        final List<Runnable> rollbackCallbacks = new ArrayList<>();
        org.mockito.Mockito.doAnswer(invocation -> {
            rollbackCallbacks.add(invocation.getArgument(0));
            return null;
        }).when(transactionManager).addRollbackCallback(any());

        // When
        persistenceFacade.save(dto);
        assertEquals(1L, dto.id);

        // Simulate rollback
        rollbackCallbacks.forEach(Runnable::run);

        // Then
        assertNull(dto.id);
    }

    @Test
    void save_withManyToOne() throws SQLException {
        // Given
        final CustomerDto customer = new CustomerDto();
        customer.name = "cust";

        final OrderDto order = new OrderDto();
        order.orderNo = "ORD1";
        order.customer = customer;

        final OrmTable customerTable = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        final OrmTable orderTable = createOrmTable(changeTracker, OrderDto.class, "orders", Map.of("id", numeric("ID"), "orderNo", varchar("ORDER_NO"), "customer", numeric("CUST_ID")), List.of("ID"));

        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(customerTable);
        when(tableRegistry.getOrmTableOrThrow(OrderDto.class)).thenReturn(orderTable);

        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenAnswer(invocation -> {
            final PreparedSql preparedSql = invocation.getArgument(0);

            if (preparedSql.sql().contains("customers")) {
                return new InsertResult(1, Map.of(customerTable.getMetaData().column("ID"), 1L));
            }
            return new InsertResult(1);
        });

        // When
        persistenceFacade.save(order);

        // Then
        verify(databaseProvider).executeUpdate(argThat(i -> i.sql().contains("customers")), eq(InsertResult.class), any());
        verify(databaseProvider).executeUpdate(argThat(i -> i.sql().contains("orders")), eq(InsertResult.class), any());
    }

    @Test
    void save_withOneToMany() throws SQLException {
        // Given
        final CategoryDto category = new CategoryDto();
        category.name = "cat";
        final ProductDto product = new ProductDto();
        product.name = "prod";
        category.products = new ArrayList<>(List.of(product));
        product.category = category;

        final FieldAccessor productDtoIdFieldAccessor = changeTracker.classFieldAccessorCache().fieldAccessor(ProductDto.class, "id");
        final FieldAccessor categoryDtoProductsFieldAccessor = changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products");
        final OrmTable categoryTable = createOrmTable(changeTracker,
                CategoryDto.class,
                "categories",
                Map.of("id", numeric("ID"),
                        "name", varchar("NAME"),
                        "products", new MappedOneToMany(productDtoIdFieldAccessor, categoryDtoProductsFieldAccessor)),
                List.of("ID"));
        final OrmTable productTable = createOrmTable(changeTracker,
                ProductDto.class,
                "products",
                Map.of("id", numeric("ID"),
                        "name", varchar("NAME"),
                        "category", numeric("CAT_ID")),
                List.of("ID"));

        when(databaseProvider.typeConverter()).thenReturn(new DefaultTypeConverter());
        when(tableRegistry.getOrmTableOrThrow(CategoryDto.class)).thenReturn(categoryTable);
        when(tableRegistry.getOrmTableOrThrow(ProductDto.class)).thenReturn(productTable);

        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenAnswer(invocation -> {
            final PreparedSql preparedSql = invocation.getArgument(0);

            if (preparedSql.sql().contains("categories")) {
                return new InsertResult(1, Map.of(categoryTable.getMetaData().column("ID"), 1L));
            }
            return new InsertResult(1);
        });

        // When
        persistenceFacade.save(category);

        // Then
        verify(databaseProvider).executeUpdate(argThat(i -> i.sql().contains("categories")), eq(InsertResult.class), any());
        verify(databaseProvider).executeUpdate(argThat(i -> i.sql().contains("products")), eq(InsertResult.class), any());
    }

    @Test
    void save_withManyToMany() throws SQLException {
        // Given
        final ProductDto product = new ProductDto();
        product.id = 1L;
        product.name = "prod";
        final TagDto tag = new TagDto();
        tag.id = 1L;
        tag.name = "tag";
        product.tags = new ArrayList<>(List.of(tag));

        final OrmTable productTable = createOrmTable(changeTracker, ProductDto.class, "products", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        productTable.syncPersistedDto(product);

        final Class<?> joinTableClass = Proxy.newProxyInstance(HiddenJoinEntity.class.getClassLoader(), new Class<?>[]{HiddenJoinEntity.class}, (proxy, method, args) -> null).getClass();
        final OrmTable joinTable = createOrmTable(changeTracker, joinTableClass, "product_tags", Map.of("prod_id", numeric("PROD_ID"), "tag_id", numeric("TAG_ID")), List.of());
        when(tableRegistry.getOrmTableOrThrow(joinTableClass)).thenReturn(joinTable);
        final MappedManyToMany m2m = new MappedManyToMany(joinTable, new String[]{"PROD_ID"}, changeTracker.classFieldAccessorCache().fieldAccessor(ProductDto.class, "tags"), null, new String[]{"TAG_ID"});

        final Map<String, Object> productFields = new HashMap<>();
        productFields.put("id", numeric("ID"));
        productFields.put("name", varchar("NAME"));
        productFields.put("tags", m2m);
        final OrmTable productTableWithM2M = createOrmTable(changeTracker, ProductDto.class, "products", productFields, List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(ProductDto.class)).thenReturn(productTableWithM2M);
        productTableWithM2M.syncPersistedDto(product);
        productTableWithM2M.trackDto(product);

        // Trigger change in collection
        final TagDto tag2 = new TagDto();
        tag2.name = "tag2";
        product.tags.add(tag2); // This will be the second tag, but for simplicity let's just say we added one

        final OrmTable tagTable = createOrmTable(changeTracker, TagDto.class, "tags", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));

        when(tableRegistry.getOrmTableOrThrow(ProductDto.class)).thenReturn(productTableWithM2M);
        when(tableRegistry.getOrmTableOrThrow(TagDto.class)).thenReturn(tagTable);

        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1));

        // When
        persistenceFacade.save(product);

        // Then
        verify(databaseProvider, times(2)).executeUpdate(any(), eq(InsertResult.class), any());
    }

    @Test
    void updateOneToManyReverseMappings() throws SQLException {
        // Given
        final CategoryDto category = new CategoryDto();
        category.name = "cat";

        final ProductDto product = new ProductDto();
        product.name = "prod";
        product.category = category;

        final OrmTable categoryTable = createOrmTable(changeTracker, CategoryDto.class, "categories", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        final OrmTable productTable = createOrmTable(changeTracker, ProductDto.class, "products", Map.of("id", numeric("ID"), "name", varchar("NAME"), "category", numeric("CAT_ID")), List.of("ID"));
        productTable.addOneToManyReverseMapping(changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products"));

        when(tableRegistry.getOrmTableOrThrow(CategoryDto.class)).thenReturn(categoryTable);
        when(tableRegistry.getOrmTableOrThrow(ProductDto.class)).thenReturn(productTable);
        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1));

        // When
        persistenceFacade.save(product);

        // Then
        assertNotNull(category.products);
        assertTrue(category.products.contains(product));
    }

    @Test
    void updateOneToManyReverseMappings_immutableCollectionOnMutableClass() throws SQLException {
        // Given
        final ProductDto existingProduct = new ProductDto();
        existingProduct.id = 1L;
        existingProduct.name = "existing";

        final CategoryDto category = new CategoryDto();
        category.id = 10L;
        category.name = "cat";
        category.products = List.of(existingProduct);

        final ProductDto newProduct = new ProductDto();
        newProduct.name = "new_prod";
        newProduct.category = category;

        final OrmTable categoryTable = createOrmTable(changeTracker, CategoryDto.class, "categories", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        categoryTable.syncPersistedDto(category);
        final OrmTable productTable = createOrmTable(changeTracker, ProductDto.class, "products", Map.of("id", numeric("ID"), "name", varchar("NAME"), "category", numeric("CAT_ID")), List.of("ID"));
        productTable.addOneToManyReverseMapping(changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products"));

        when(tableRegistry.getOrmTableOrThrow(CategoryDto.class)).thenReturn(categoryTable);
        when(tableRegistry.getOrmTableOrThrow(ProductDto.class)).thenReturn(productTable);
        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1));

        // When
        persistenceFacade.save(newProduct);

        // Then
        assertNotNull(category.products);
        assertTrue(category.products.contains(existingProduct));
        assertTrue(category.products.contains(newProduct));
    }

    @Test
    void updateOneToManyReverseMappings_recordWithNullCollection() throws SQLException {
        // Given
        final DepartmentRecord department = new DepartmentRecord(100L, "Engineering", null);

        final EmployeeDto employee = new EmployeeDto();
        employee.name = "Alice";
        employee.department = department;

        final OrmTable departmentTable = createOrmTable(changeTracker, DepartmentRecord.class, "departments", Map.of("id", numeric("ID"), "name", varchar("NAME"), "employees", new MappedOneToMany(null, changeTracker.classFieldAccessorCache().fieldAccessor(DepartmentRecord.class, "employees"))), List.of("ID"));
        departmentTable.syncPersistedDto(department);
        final OrmTable employeeTable = createOrmTable(changeTracker, EmployeeDto.class, "employees", Map.of("id", numeric("ID"), "name", varchar("NAME"), "department", numeric("DEPT_ID")), List.of("ID"));
        employeeTable.addOneToManyReverseMapping(changeTracker.classFieldAccessorCache().fieldAccessor(DepartmentRecord.class, "employees"));

        when(tableRegistry.getOrmTableOrThrow(DepartmentRecord.class)).thenReturn(departmentTable);
        when(tableRegistry.getOrmTableOrThrow(EmployeeDto.class)).thenReturn(employeeTable);
        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1));

        // When
        persistenceFacade.save(employee);

        // Then
        final Set<org.litebridge.tracking.TrackedDto<DepartmentRecord>> trackedDepts = changeTracker.getTrackedDtos(DepartmentRecord.class);
        assertEquals(2, trackedDepts.size());
        final DepartmentRecord updatedDept = trackedDepts.stream().map(org.litebridge.tracking.TrackedDto::dto).filter(d -> d.employees() != null).findFirst().orElseThrow();
        assertNotNull(updatedDept.employees());
        assertTrue(updatedDept.employees().contains(employee));
    }

    @Test
    void updateOneToManyReverseMappings_recordWithImmutableCollection() throws SQLException {
        // Given
        final EmployeeDto emp1 = new EmployeeDto();
        emp1.id = 1L;
        emp1.name = "Alice";

        final DepartmentRecord department = new DepartmentRecord(100L, "Engineering", List.of(emp1));

        final EmployeeDto emp2 = new EmployeeDto();
        emp2.name = "Bob";
        emp2.department = department;

        final OrmTable departmentTable = createOrmTable(changeTracker, DepartmentRecord.class, "departments", Map.of("id", numeric("ID"), "name", varchar("NAME"), "employees", new MappedOneToMany(null, changeTracker.classFieldAccessorCache().fieldAccessor(DepartmentRecord.class, "employees"))), List.of("ID"));
        departmentTable.syncPersistedDto(department);
        final OrmTable employeeTable = createOrmTable(changeTracker, EmployeeDto.class, "employees", Map.of("id", numeric("ID"), "name", varchar("NAME"), "department", numeric("DEPT_ID")), List.of("ID"));
        employeeTable.addOneToManyReverseMapping(changeTracker.classFieldAccessorCache().fieldAccessor(DepartmentRecord.class, "employees"));

        when(tableRegistry.getOrmTableOrThrow(DepartmentRecord.class)).thenReturn(departmentTable);
        when(tableRegistry.getOrmTableOrThrow(EmployeeDto.class)).thenReturn(employeeTable);
        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1));

        // When
        persistenceFacade.save(emp2);

        // Then
        final Set<org.litebridge.tracking.TrackedDto<DepartmentRecord>> trackedDepts = changeTracker.getTrackedDtos(DepartmentRecord.class);
        final DepartmentRecord updatedDept = trackedDepts.stream().map(org.litebridge.tracking.TrackedDto::dto).filter(d -> d.employees() != null && d.employees().size() == 2).findFirst().orElseThrow();
        assertTrue(updatedDept.employees().contains(emp1));
        assertTrue(updatedDept.employees().contains(emp2));
    }

    @Test
    void updateOneToManyReverseMappings_childAlreadyPresent_noOp() throws SQLException {
        // Given
        final EmployeeDto emp1 = new EmployeeDto();
        emp1.name = "Alice";

        final DepartmentRecord department = new DepartmentRecord(100L, "Engineering", List.of(emp1));
        emp1.department = department;

        final OrmTable departmentTable = createOrmTable(changeTracker, DepartmentRecord.class, "departments", Map.of("id", numeric("ID"), "name", varchar("NAME"), "employees", new MappedOneToMany(null, changeTracker.classFieldAccessorCache().fieldAccessor(DepartmentRecord.class, "employees"))), List.of("ID"));
        departmentTable.syncPersistedDto(department);
        final OrmTable employeeTable = createOrmTable(changeTracker, EmployeeDto.class, "employees", Map.of("id", numeric("ID"), "name", varchar("NAME"), "department", numeric("DEPT_ID")), List.of("ID"));
        employeeTable.addOneToManyReverseMapping(changeTracker.classFieldAccessorCache().fieldAccessor(DepartmentRecord.class, "employees"));

        when(tableRegistry.getOrmTableOrThrow(DepartmentRecord.class)).thenReturn(departmentTable);
        when(tableRegistry.getOrmTableOrThrow(EmployeeDto.class)).thenReturn(employeeTable);
        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1));

        // When
        persistenceFacade.save(emp1);

        // Then
        final Set<org.litebridge.tracking.TrackedDto<DepartmentRecord>> trackedDepts = changeTracker.getTrackedDtos(DepartmentRecord.class);
        assertEquals(1, trackedDepts.size());
        assertEquals(department, trackedDepts.iterator().next().dto());
    }

    @Test
    void updateOneToManyReverseMappings_rollbackForImmutableCollection() throws SQLException {
        // Given
        final ProductDto existingProduct = new ProductDto();
        existingProduct.id = 1L;
        existingProduct.name = "existing";

        final List<ProductDto> initialList = List.of(existingProduct);
        final CategoryDto category = new CategoryDto();
        category.id = 10L;
        category.name = "cat";
        category.products = initialList;

        final ProductDto newProduct = new ProductDto();
        newProduct.name = "new_prod";
        newProduct.category = category;

        final OrmTable categoryTable = createOrmTable(changeTracker, CategoryDto.class, "categories", Map.of("id", numeric("ID"), "name", varchar("NAME"), "products", new MappedOneToMany(null, changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products"))), List.of("ID"));
        categoryTable.syncPersistedDto(category);
        final OrmTable productTable = createOrmTable(changeTracker, ProductDto.class, "products", Map.of("id", numeric("ID"), "name", varchar("NAME"), "category", numeric("CAT_ID")), List.of("ID"));
        productTable.addOneToManyReverseMapping(changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products"));

        when(tableRegistry.getOrmTableOrThrow(CategoryDto.class)).thenReturn(categoryTable);
        when(tableRegistry.getOrmTableOrThrow(ProductDto.class)).thenReturn(productTable);
        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1));

        final List<Runnable> rollbackCallbacks = new ArrayList<>();
        org.mockito.Mockito.doAnswer(invocation -> {
            rollbackCallbacks.add(invocation.getArgument(0));
            return null;
        }).when(transactionManager).addRollbackCallback(any());

        // When
        persistenceFacade.save(newProduct);
        rollbackCallbacks.forEach(Runnable::run);

        // Then
        assertEquals(initialList, category.products);
    }

    @Test
    void updateOneToManyReverseMappings_rollbackForRecord() throws SQLException {
        // Given
        final EmployeeDto emp1 = new EmployeeDto();
        emp1.id = 1L;
        emp1.name = "Alice";

        final DepartmentRecord initialDepartment = new DepartmentRecord(100L, "Engineering", List.of(emp1));

        final EmployeeDto emp2 = new EmployeeDto();
        emp2.name = "Bob";
        emp2.department = initialDepartment;

        final OrmTable departmentTable = createOrmTable(changeTracker, DepartmentRecord.class, "departments", Map.of("id", numeric("ID"), "name", varchar("NAME"), "employees", new MappedOneToMany(null, changeTracker.classFieldAccessorCache().fieldAccessor(DepartmentRecord.class, "employees"))), List.of("ID"));
        departmentTable.syncPersistedDto(initialDepartment);
        final OrmTable employeeTable = createOrmTable(changeTracker, EmployeeDto.class, "employees", Map.of("id", numeric("ID"), "name", varchar("NAME"), "department", numeric("DEPT_ID")), List.of("ID"));
        employeeTable.addOneToManyReverseMapping(changeTracker.classFieldAccessorCache().fieldAccessor(DepartmentRecord.class, "employees"));

        when(tableRegistry.getOrmTableOrThrow(DepartmentRecord.class)).thenReturn(departmentTable);
        when(tableRegistry.getOrmTableOrThrow(EmployeeDto.class)).thenReturn(employeeTable);
        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1));

        final List<Runnable> rollbackCallbacks = new ArrayList<>();
        org.mockito.Mockito.doAnswer(invocation -> {
            rollbackCallbacks.add(invocation.getArgument(0));
            return null;
        }).when(transactionManager).addRollbackCallback(any());

        // When
        persistenceFacade.save(emp2);
        rollbackCallbacks.forEach(Runnable::run);

        // Then
        assertTrue(departmentTable.isPersistedDto(initialDepartment));
    }

    @Test
    void delete_withNullPk() throws SQLException {
        // Given
        final CustomerDto dto = new CustomerDto();
        dto.id = null;

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID")), List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.executeUpdate(any(), eq(UpdateResult.class), any())).thenReturn(new UpdateResult(1));

        // When
        persistenceFacade.delete(dto);

        // Then
        verify(databaseProvider).executeUpdate(argThat(po -> po.sql().contains("IS NULL")), eq(UpdateResult.class), any());
    }

    @Test
    void update_noChanges() throws SQLException {
        // Given
        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "test";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        table.syncPersistedDto(dto);

        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);

        // When
        persistenceFacade.update(dto);

        // Then
        verify(databaseProvider, never()).executeUpdate(any(), eq(UpdateResult.class), any());
    }

    @Test
    void insert_withExplicitPk() throws SQLException {
        // Given
        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "test";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"), Collections.emptySet());
        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1));

        // When
        persistenceFacade.insert(dto);

        // Then
        verify(databaseProvider).executeUpdate(argThat(i -> i.updateMetaData() != null && !i.updateMetaData().returnGeneratedKeys()), eq(InsertResult.class), any());
    }

    @Test
    void save_withNoOpFieldAccessor() throws SQLException {
        // Given
        final CustomerDto dto = new CustomerDto();

        final Map<String, Object> fields = new HashMap<>();
        fields.put("id", numeric("ID"));
        fields.put("name", new NoOpFieldAccessor());

        final OrmTable ormTable = createOrmTable(changeTracker, CustomerDto.class, "customers", fields, List.of("ID"));
        ormTable.trackDto(dto);
        dto.id = 1L;
        dto.name = "test";

        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(ormTable);
        when(tableRegistry.getOrmTableOrThrow(any(Table.class))).thenReturn(ormTable);
        when(databaseProvider.executeUpdate(any(), eq(UpdateResult.class), any())).thenReturn(new UpdateResult(1));

        // When
        persistenceFacade.save(dto);

        // Then
        verify(databaseProvider).executeUpdate(argThat(i -> i.bindValues().size() == 2), eq(UpdateResult.class), any());
    }

    @Test
    void save_withDeeplyNestedGeneratedKeys() throws SQLException {
        // Given
        final CategoryDto category = new CategoryDto();
        category.name = "cat";
        final ProductDto product = new ProductDto();
        product.name = "prod";
        category.products = new ArrayList<>(List.of(product));
        product.category = category;

        final FieldAccessor productDtoIdFieldAccessor = changeTracker.classFieldAccessorCache().fieldAccessor(ProductDto.class, "id");
        final FieldAccessor categoryDtoProductsFieldAccessor = changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products");
        final OrmTable categoryTable = createOrmTable(changeTracker,
                CategoryDto.class,
                "categories",
                Map.of("id", numeric("ID"),
                        "name", varchar("NAME"),
                        "products", new MappedOneToMany(productDtoIdFieldAccessor, categoryDtoProductsFieldAccessor)),
                List.of("ID"));
        final OrmTable productTable = createOrmTable(changeTracker, ProductDto.class, "products", Map.of("id", numeric("ID"), "name", varchar("NAME"), "category", numeric("CAT_ID")), List.of("ID"));

        when(tableRegistry.getOrmTableOrThrow(CategoryDto.class)).thenReturn(categoryTable);
        when(tableRegistry.getOrmTableOrThrow(ProductDto.class)).thenReturn(productTable);

        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenAnswer(invocation -> {
            final PreparedSql preparedSql = invocation.getArgument(0);
            if (preparedSql.sql().contains("categories")) {
                return new InsertResult(1, Map.of(categoryTable.getMetaData().column("ID"), 10L));
            } else if (preparedSql.sql().contains("products")) {
                return new InsertResult(1, Map.of(productTable.getMetaData().column("ID"), 20L));
            }
            return new InsertResult(1);
        });

        // When
        persistenceFacade.save(category);

        // Then
        assertEquals(10L, category.id);
        assertEquals(20L, product.id);
    }

    @Test
    void merge_withGeneratedKeys() throws SQLException {
        // Given
        final CustomerDto dto = new CustomerDto();
        dto.name = "new customer";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        when(databaseProvider.executeUpdate(any(), eq(UpdateResult.class), any()))
                .thenReturn(new InsertResult(1, Map.of(table.getMetaData().column("ID"), 99L)));

        // When
        persistenceFacade.merge(dto);

        // Then
        assertEquals(99L, dto.id);
        assertTrue(table.isPersistedDto(dto));
        verify(databaseProvider).executeUpdate(any(), eq(UpdateResult.class), any());
    }

    @Test
    void merge_withOneToManyReverseMappings() throws SQLException {
        // Given
        final CategoryDto category = new CategoryDto();
        category.id = 1L;
        category.name = "Electronics";

        final ProductDto product = new ProductDto();
        product.name = "Phone";
        category.products = new ArrayList<>(List.of(product));

        final OrmTable categoryTable = createOrmTable(changeTracker, CategoryDto.class, "categories",
                Map.of("id", numeric("ID"), "name", varchar("NAME"), "products", new MappedOneToMany(
                        changeTracker.classFieldAccessorCache().fieldAccessor(ProductDto.class, "category"),
                        changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products")
                )), List.of("ID"));

        final OrmTable productTable = createOrmTable(changeTracker, ProductDto.class, "products",
                Map.of("id", numeric("ID"), "name", varchar("NAME"), "category", numeric("CATEGORY_ID")), List.of("ID"));
        productTable.addOneToManyReverseMapping(changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products"));

        when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        lenient().when(databaseProvider.executeUpdate(any(), any(), any())).thenReturn(new InsertResult(1));

        // When
        persistenceFacade.merge(category);

        // Then
        verify(databaseProvider, times(2)).executeUpdate(any(), any(), any());
    }

    @Test
    void merge_manualUpsert_updateSucceeds() throws SQLException {
        // Given
        final TransactionalDatabaseProvider customDbProvider = mock(TransactionalDatabaseProvider.class);
        final DatabaseProviderMetaData notSupportedMetaData = new DatabaseProviderMetaData(true,
                DatabaseProviderMetaData.MergeCapability.NOT_SUPPORTED,
                DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW);
        when(customDbProvider.metaData()).thenReturn(notSupportedMetaData);
        final PersistenceFacade facade = createFacade(tableRegistry, customDbProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "Updated Customer";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        table.trackDto(dto);
        dto.name = "Changed Name";

        when(customDbProvider.transactionManager()).thenReturn(transactionManager);
        when(customDbProvider.executeUpdate(any(), eq(UpdateResult.class), any())).thenReturn(new UpdateResult(1));

        // When
        facade.save(dto);

        // Then
        verify(customDbProvider).executeUpdate(any(), eq(UpdateResult.class), any());
        verify(customDbProvider, never()).executeUpdate(any(), eq(InsertResult.class), any());
    }

    @Test
    void merge_manualUpsert_insertFallback() throws SQLException {
        // Given
        final TransactionalDatabaseProvider customDbProvider = mock(TransactionalDatabaseProvider.class);
        final DatabaseProviderMetaData notSupportedMetaData = new DatabaseProviderMetaData(true,
                DatabaseProviderMetaData.MergeCapability.NOT_SUPPORTED,
                DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW);
        when(customDbProvider.metaData()).thenReturn(notSupportedMetaData);
        final PersistenceFacade facade = createFacade(tableRegistry, customDbProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "New Customer";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));

        when(customDbProvider.transactionManager()).thenReturn(transactionManager);
        when(customDbProvider.executeUpdate(any(), eq(UpdateResult.class), any())).thenReturn(new UpdateResult(0));
        when(customDbProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenReturn(new InsertResult(1));

        // When
        facade.save(dto);

        // Then
        verify(customDbProvider).executeUpdate(any(), eq(UpdateResult.class), any());
        verify(customDbProvider).executeUpdate(any(), eq(InsertResult.class), any());
    }

    @Test
    void insert_noOpStatement_whenNoChanges() throws SQLException {
        // Given
        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "name";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        table.syncPersistedDto(dto);

        // When - update with no changes produces NO_OP_STATEMENT_BUILDER
        persistenceFacade.update(dto);

        // Then
        verify(databaseProvider, never()).executeUpdate(any(), any(), any());
    }

    @Test
    void update_untrackedDto_ensuresTracked() throws SQLException {
        // Given
        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "name";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        when(databaseProvider.executeUpdate(any(), eq(UpdateResult.class), any())).thenReturn(new UpdateResult(1));

        // When
        persistenceFacade.update(dto);

        // Then
        verify(databaseProvider).executeUpdate(any(), eq(UpdateResult.class), any());
    }

    @Test
    void prepareUpdateStatement_withNoOpFieldAccessor() throws SQLException {
        // Given
        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "name";

        final Table table = new Table("", "public", "customers");
        final ColumnMetaData idCol = new ColumnMetaData(table, "ID", false, Types.NUMERIC);
        final ColumnMetaData nameCol = new ColumnMetaData(table, "NAME", false, Types.VARCHAR);
        final ColumnMetaData noopCol = new ColumnMetaData(table, "NOOP_COL", true, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(table, List.of("ID"), List.of(idCol, nameCol, noopCol));
        metaDataMap.put(table.qualifiedName(), metaData);

        final FieldAccessor idAccessor = changeTracker.classFieldAccessorCache().fieldAccessor(CustomerDto.class, "id");
        final FieldAccessor nameAccessor = changeTracker.classFieldAccessorCache().fieldAccessor(CustomerDto.class, "name");

        final Map<FieldAccessor, MappedFieldTarget> fieldTargetMap = new HashMap<>();
        fieldTargetMap.put(idAccessor, idCol);
        fieldTargetMap.put(nameAccessor, nameCol);
        fieldTargetMap.put(new NoOpFieldAccessor(), noopCol);

        final OrmTable ormTable = new OrmTable(CustomerDto.class, metaData, fieldTargetMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        changeTracker.trackDtoFields(dto, Set.of(idAccessor, nameAccessor), true);
        dto.name = "changed";

        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(ormTable);
        when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        when(databaseProvider.executeUpdate(any(), eq(UpdateResult.class), any())).thenReturn(new UpdateResult(1));

        // When
        persistenceFacade.update(dto);

        // Then
        verify(databaseProvider).executeUpdate(any(), eq(UpdateResult.class), any());
    }

    @Test
    void processRelatedDto_withColumnAndInlineTable() throws SQLException {
        // Given
        final OrderDto order = new OrderDto();
        order.orderNo = "ORD-1";
        final CustomerDto customer = new CustomerDto();
        customer.name = "Inline customer";
        order.customer = customer;

        final Table custTable = new Table("", "public", "customers");
        final ColumnMetaData custIdCol = new ColumnMetaData(custTable, "ID", false, Types.NUMERIC, 0, 0, true, null, null);
        final ColumnMetaData custNameCol = new ColumnMetaData(custTable, "NAME", false, Types.VARCHAR);
        final TableMetaData custMeta = new TableMetaData(custTable, List.of("ID"), List.of(custIdCol, custNameCol));
        metaDataMap.put(custTable.qualifiedName(), custMeta);

        final OrmTable custOrmTable = new OrmTable(CustomerDto.class, custMeta, Map.of(
                changeTracker.classFieldAccessorCache().fieldAccessor(CustomerDto.class, "id"), custIdCol,
                changeTracker.classFieldAccessorCache().fieldAccessor(CustomerDto.class, "name"), custNameCol
        ), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));

        final Table orderTable = new Table("", "public", "orders");
        final ColumnMetaData orderIdCol = new ColumnMetaData(orderTable, "ID", false, Types.NUMERIC, 0, 0, true, null, null);
        final ColumnMetaData orderNoCol = new ColumnMetaData(orderTable, "ORDER_NO", false, Types.VARCHAR);
        final ColumnMetaData orderCustIdCol = new ColumnMetaData(orderTable, "CUST_ID", false, Types.NUMERIC);
        orderCustIdCol.setJoinColumnSupplier(() -> custIdCol);
        final TableMetaData orderMeta = new TableMetaData(orderTable, List.of("ID"), List.of(orderIdCol, orderNoCol, orderCustIdCol));
        metaDataMap.put(orderTable.qualifiedName(), orderMeta);

        final ColumnAndInlineTable columnAndInlineTable = new ColumnAndInlineTable(orderCustIdCol, custOrmTable);

        final OrmTable orderOrmTable = new OrmTable(OrderDto.class, orderMeta, Map.of(
                changeTracker.classFieldAccessorCache().fieldAccessor(OrderDto.class, "id"), orderIdCol,
                changeTracker.classFieldAccessorCache().fieldAccessor(OrderDto.class, "orderNo"), orderNoCol,
                changeTracker.classFieldAccessorCache().fieldAccessor(OrderDto.class, "customer"), columnAndInlineTable
        ), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));

        when(tableRegistry.getOrmTableOrThrow(OrderDto.class)).thenReturn(orderOrmTable);
        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(custOrmTable);
        when(databaseProvider.transactionManager()).thenReturn(transactionManager);

        when(databaseProvider.executeUpdate(any(), eq(InsertResult.class), any())).thenAnswer(inv -> {
            final PreparedSql ps = inv.getArgument(0);
            if (ps.sql().contains("customers")) {
                return new InsertResult(1, Map.of(custIdCol, 55L));
            }
            return new InsertResult(1);
        });

        // When
        persistenceFacade.save(order);

        // Then
        assertEquals(55L, customer.id);
        verify(databaseProvider, times(2)).executeUpdate(any(), eq(InsertResult.class), any());
    }

    @Test
    void processRelatedDto_withMappedCompositeKey() throws SQLException {
        // Given
        final CompositeParentEntity parent = new CompositeParentEntity();
        final CompositeChildDto child = new CompositeChildDto();
        child.keyPartA = 100L;
        child.keyPartB = "B1";
        child.detail = "child detail";
        parent.child = child;

        final Table childTable = new Table("", "public", "composite_child");
        final ColumnMetaData childPkA = new ColumnMetaData(childTable, "KEY_A", false, Types.NUMERIC);
        final ColumnMetaData childPkB = new ColumnMetaData(childTable, "KEY_B", false, Types.VARCHAR);
        final ColumnMetaData childDetail = new ColumnMetaData(childTable, "DETAIL", false, Types.VARCHAR);
        final TableMetaData childMeta = new TableMetaData(childTable, List.of("KEY_A", "KEY_B"), List.of(childPkA, childPkB, childDetail));
        metaDataMap.put(childTable.qualifiedName(), childMeta);

        final OrmTable childOrmTable = new OrmTable(CompositeChildDto.class, childMeta, Map.of(
                changeTracker.classFieldAccessorCache().fieldAccessor(CompositeChildDto.class, "keyPartA"), childPkA,
                changeTracker.classFieldAccessorCache().fieldAccessor(CompositeChildDto.class, "keyPartB"), childPkB,
                changeTracker.classFieldAccessorCache().fieldAccessor(CompositeChildDto.class, "detail"), childDetail
        ), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));

        final Table parentTable = new Table("", "public", "composite_parent");
        final ColumnMetaData parentId = new ColumnMetaData(parentTable, "ID", false, Types.NUMERIC, 0, 0, true, null, null);
        final ColumnMetaData parentFkA = new ColumnMetaData(parentTable, "FK_A", false, Types.NUMERIC);
        final ColumnMetaData parentFkB = new ColumnMetaData(parentTable, "FK_B", false, Types.VARCHAR);
        final TableMetaData parentMeta = new TableMetaData(parentTable, List.of("ID"), List.of(parentId, parentFkA, parentFkB));
        metaDataMap.put(parentTable.qualifiedName(), parentMeta);

        final MappedCompositeKey mappedCompositeKey = new MappedCompositeKey(
                new MappedFieldTarget[]{parentFkA, parentFkB},
                () -> childOrmTable,
                new String[]{"KEY_A", "KEY_B"}
        );

        final OrmTable parentOrmTable = new OrmTable(CompositeParentEntity.class, parentMeta, Map.of(
                changeTracker.classFieldAccessorCache().fieldAccessor(CompositeParentEntity.class, "id"), parentId,
                changeTracker.classFieldAccessorCache().fieldAccessor(CompositeParentEntity.class, "child"), mappedCompositeKey
        ), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));

        lenient().when(tableRegistry.getOrmTable(CompositeParentEntity.class)).thenReturn(parentOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(CompositeParentEntity.class)).thenReturn(parentOrmTable);
        lenient().when(tableRegistry.getOrmTable(parentTable)).thenReturn(parentOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(parentTable)).thenReturn(parentOrmTable);
        lenient().when(tableRegistry.getOrmTable(parentTable.name())).thenReturn(parentOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(parentTable.name())).thenReturn(parentOrmTable);
        lenient().when(tableRegistry.getOrmTable(parentTable.qualifiedName())).thenReturn(parentOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(parentTable.qualifiedName())).thenReturn(parentOrmTable);

        lenient().when(tableRegistry.getOrmTable(CompositeChildDto.class)).thenReturn(childOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(CompositeChildDto.class)).thenReturn(childOrmTable);
        lenient().when(tableRegistry.getOrmTable(childTable)).thenReturn(childOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(childTable)).thenReturn(childOrmTable);
        lenient().when(tableRegistry.getOrmTable(childTable.name())).thenReturn(childOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(childTable.name())).thenReturn(childOrmTable);
        lenient().when(tableRegistry.getOrmTable(childTable.qualifiedName())).thenReturn(childOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(childTable.qualifiedName())).thenReturn(childOrmTable);

        when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        lenient().when(databaseProvider.executeUpdate(any(), any(), any())).thenReturn(new InsertResult(1));

        // When
        persistenceFacade.save(parent);

        // Then
        verify(databaseProvider, times(2)).executeUpdate(any(), any(), any());
    }

    @Test
    void processOneToMany_compositePk_throwsUnsupportedOperationException() throws SQLException {
        // Given
        final CategoryDto category = new CategoryDto();
        category.name = "Cat";
        final ProductDto product = new ProductDto();
        product.name = "Prod";
        category.products = List.of(product);

        // Parent category table has composite primary key
        final OrmTable categoryTable = createOrmTable(changeTracker, CategoryDto.class, "categories",
                Map.of("id", numeric("ID"), "name", varchar("NAME"), "products", new MappedOneToMany(
                        changeTracker.classFieldAccessorCache().fieldAccessor(ProductDto.class, "category"),
                        changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products")
                )), List.of("ID", "NAME"));

        final OrmTable productTable = createOrmTable(changeTracker, ProductDto.class, "products",
                Map.of("id", numeric("ID"), "name", varchar("NAME"), "category", numeric("CATEGORY_ID")), List.of("ID"));

        when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        when(databaseProvider.executeUpdate(any(), any(), any())).thenReturn(new InsertResult(1));

        // When / Then
        assertThrows(UnsupportedOperationException.class, () -> persistenceFacade.save(category));
    }

    @Test
    void tableProvider_throwsIllegalArgumentException_whenTableNotFound() {
        // Given
        final TableRegistry mockRegistry = mock(TableRegistry.class);
        when(mockRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(null);
        final PersistenceFacade.TableProvider tableProvider = new PersistenceFacade.TableProvider(mockRegistry);

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> tableProvider.getTableOrThrow(CustomerDto.class));
    }

    @Test
    void update_withCompositePrimaryKey_generatesCompositeConditions() throws SQLException {
        // Given
        final CompositePkEntity entity = new CompositePkEntity();
        final CompositeChildDto pk = new CompositeChildDto();
        pk.keyPartA = 1L;
        pk.keyPartB = "K2";
        entity.pk = pk;
        entity.name = "initial";

        final Table pkTable = new Table("", "public", "composite_pk_sub");
        final ColumnMetaData pkColA = new ColumnMetaData(pkTable, "KEY_A", false, Types.NUMERIC);
        final ColumnMetaData pkColB = new ColumnMetaData(pkTable, "KEY_B", false, Types.VARCHAR);
        final TableMetaData pkMeta = new TableMetaData(pkTable, List.of("KEY_A", "KEY_B"), List.of(pkColA, pkColB));
        metaDataMap.put(pkTable.qualifiedName(), pkMeta);

        final OrmTable pkOrmTable = new OrmTable(CompositeChildDto.class, pkMeta, Map.of(
                changeTracker.classFieldAccessorCache().fieldAccessor(CompositeChildDto.class, "keyPartA"), pkColA,
                changeTracker.classFieldAccessorCache().fieldAccessor(CompositeChildDto.class, "keyPartB"), pkColB
        ), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));

        final Table table = new Table("", "public", "composite_pk_entity");
        final ColumnMetaData pkFieldCol = new ColumnMetaData(table, "PK", false, Types.OTHER);
        final ColumnMetaData nameCol = new ColumnMetaData(table, "NAME", false, Types.VARCHAR);
        final TableMetaData metaData = new TableMetaData(table, List.of("PK"), List.of(pkFieldCol, nameCol));
        metaDataMap.put(table.qualifiedName(), metaData);

        final OrmTable ormTable = new OrmTable(CompositePkEntity.class, metaData, Map.of(
                changeTracker.classFieldAccessorCache().fieldAccessor(CompositePkEntity.class, "pk"), pkFieldCol,
                changeTracker.classFieldAccessorCache().fieldAccessor(CompositePkEntity.class, "name"), nameCol
        ), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));

        ormTable.trackDto(entity);
        entity.name = "modified";

        lenient().when(tableRegistry.getOrmTable(CompositePkEntity.class)).thenReturn(ormTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(CompositePkEntity.class)).thenReturn(ormTable);
        lenient().when(tableRegistry.getOrmTable(table)).thenReturn(ormTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(table)).thenReturn(ormTable);

        lenient().when(tableRegistry.getOrmTable(CompositeChildDto.class)).thenReturn(pkOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(CompositeChildDto.class)).thenReturn(pkOrmTable);
        lenient().when(tableRegistry.getOrmTable(pkTable)).thenReturn(pkOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(pkTable)).thenReturn(pkOrmTable);

        when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        when(databaseProvider.executeUpdate(any(), eq(UpdateResult.class), any())).thenReturn(new UpdateResult(1));

        // When
        persistenceFacade.update(entity);

        // Then
        verify(databaseProvider).executeUpdate(any(), eq(UpdateResult.class), any());
    }

    @Test
    void processRelatedDto_withMappedCompositeKey_update() throws SQLException {
        // Given
        final CompositeParentEntity parent = new CompositeParentEntity();
        parent.id = 1L;
        final CompositeChildDto child = new CompositeChildDto();
        child.keyPartA = 100L;
        child.keyPartB = "B1";
        child.detail = "child detail";
        parent.child = child;

        final Table childTable = new Table("", "public", "composite_child");
        final ColumnMetaData childPkA = new ColumnMetaData(childTable, "KEY_A", false, Types.NUMERIC);
        final ColumnMetaData childPkB = new ColumnMetaData(childTable, "KEY_B", false, Types.VARCHAR);
        final ColumnMetaData childDetail = new ColumnMetaData(childTable, "DETAIL", false, Types.VARCHAR);
        final TableMetaData childMeta = new TableMetaData(childTable, List.of("KEY_A", "KEY_B"), List.of(childPkA, childPkB, childDetail));
        metaDataMap.put(childTable.qualifiedName(), childMeta);

        final OrmTable childOrmTable = new OrmTable(CompositeChildDto.class, childMeta, Map.of(
                changeTracker.classFieldAccessorCache().fieldAccessor(CompositeChildDto.class, "keyPartA"), childPkA,
                changeTracker.classFieldAccessorCache().fieldAccessor(CompositeChildDto.class, "keyPartB"), childPkB,
                changeTracker.classFieldAccessorCache().fieldAccessor(CompositeChildDto.class, "detail"), childDetail
        ), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));

        final Table parentTable = new Table("", "public", "composite_parent");
        final ColumnMetaData parentId = new ColumnMetaData(parentTable, "ID", false, Types.NUMERIC, 0, 0, true, null, null);
        final ColumnMetaData parentFkA = new ColumnMetaData(parentTable, "FK_A", false, Types.NUMERIC);
        final ColumnMetaData parentFkB = new ColumnMetaData(parentTable, "FK_B", false, Types.VARCHAR);
        final TableMetaData parentMeta = new TableMetaData(parentTable, List.of("ID"), List.of(parentId, parentFkA, parentFkB));
        metaDataMap.put(parentTable.qualifiedName(), parentMeta);

        final MappedCompositeKey mappedCompositeKey = new MappedCompositeKey(
                new MappedFieldTarget[]{parentFkA, parentFkB},
                () -> childOrmTable,
                new String[]{"KEY_A", "KEY_B"}
        );

        final OrmTable parentOrmTable = new OrmTable(CompositeParentEntity.class, parentMeta, Map.of(
                changeTracker.classFieldAccessorCache().fieldAccessor(CompositeParentEntity.class, "id"), parentId,
                changeTracker.classFieldAccessorCache().fieldAccessor(CompositeParentEntity.class, "child"), mappedCompositeKey
        ), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        parent.child = null;
        parentOrmTable.trackDto(parent);
        parent.child = child;

        lenient().when(tableRegistry.getOrmTable(CompositeParentEntity.class)).thenReturn(parentOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(CompositeParentEntity.class)).thenReturn(parentOrmTable);
        lenient().when(tableRegistry.getOrmTable(parentTable)).thenReturn(parentOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(parentTable)).thenReturn(parentOrmTable);
        lenient().when(tableRegistry.getOrmTable(parentTable.name())).thenReturn(parentOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(parentTable.name())).thenReturn(parentOrmTable);
        lenient().when(tableRegistry.getOrmTable(parentTable.qualifiedName())).thenReturn(parentOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(parentTable.qualifiedName())).thenReturn(parentOrmTable);

        lenient().when(tableRegistry.getOrmTable(CompositeChildDto.class)).thenReturn(childOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(CompositeChildDto.class)).thenReturn(childOrmTable);
        lenient().when(tableRegistry.getOrmTable(childTable)).thenReturn(childOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(childTable)).thenReturn(childOrmTable);
        lenient().when(tableRegistry.getOrmTable(childTable.name())).thenReturn(childOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(childTable.name())).thenReturn(childOrmTable);
        lenient().when(tableRegistry.getOrmTable(childTable.qualifiedName())).thenReturn(childOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(childTable.qualifiedName())).thenReturn(childOrmTable);

        when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        lenient().when(databaseProvider.executeUpdate(any(), any(), any())).thenReturn(new UpdateResult(1));

        // When
        persistenceFacade.update(parent);

        // Then
        verify(databaseProvider, times(2)).executeUpdate(any(), any(), any());
    }

    @Test
    void update_referencingPersistedNestedDto() throws SQLException {
        // Given
        final OrderDto order = new OrderDto();
        order.id = 1L;
        order.orderNo = "ORD-1";
        final CustomerDto customer = new CustomerDto();
        customer.id = 5L;
        customer.name = "Existing Cust";
        order.customer = customer;

        final Table custTable = new Table("", "public", "customers");
        final ColumnMetaData custIdCol = new ColumnMetaData(custTable, "ID", false, Types.NUMERIC);
        final ColumnMetaData custNameCol = new ColumnMetaData(custTable, "NAME", false, Types.VARCHAR);
        final TableMetaData custMeta = new TableMetaData(custTable, List.of("ID"), List.of(custIdCol, custNameCol));
        metaDataMap.put(custTable.qualifiedName(), custMeta);

        final OrmTable custOrmTable = new OrmTable(CustomerDto.class, custMeta, Map.of(
                changeTracker.classFieldAccessorCache().fieldAccessor(CustomerDto.class, "id"), custIdCol,
                changeTracker.classFieldAccessorCache().fieldAccessor(CustomerDto.class, "name"), custNameCol
        ), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        custOrmTable.syncPersistedDto(customer);

        final Table orderTable = new Table("", "public", "orders");
        final ColumnMetaData orderIdCol = new ColumnMetaData(orderTable, "ID", false, Types.NUMERIC);
        final ColumnMetaData orderNoCol = new ColumnMetaData(orderTable, "ORDER_NO", false, Types.VARCHAR);
        final ColumnMetaData orderCustIdCol = new ColumnMetaData(orderTable, "CUST_ID", false, Types.NUMERIC);
        orderCustIdCol.setJoinColumnSupplier(() -> custIdCol);
        final TableMetaData orderMeta = new TableMetaData(orderTable, List.of("ID"), List.of(orderIdCol, orderNoCol, orderCustIdCol));
        metaDataMap.put(orderTable.qualifiedName(), orderMeta);

        final OrmTable orderOrmTable = new OrmTable(OrderDto.class, orderMeta, Map.of(
                changeTracker.classFieldAccessorCache().fieldAccessor(OrderDto.class, "id"), orderIdCol,
                changeTracker.classFieldAccessorCache().fieldAccessor(OrderDto.class, "orderNo"), orderNoCol,
                changeTracker.classFieldAccessorCache().fieldAccessor(OrderDto.class, "customer"), orderCustIdCol
        ), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));

        orderOrmTable.trackDto(order);
        order.orderNo = "ORD-2";

        lenient().when(tableRegistry.getOrmTable(OrderDto.class)).thenReturn(orderOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(OrderDto.class)).thenReturn(orderOrmTable);
        lenient().when(tableRegistry.getOrmTable(orderTable)).thenReturn(orderOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(orderTable)).thenReturn(orderOrmTable);

        lenient().when(tableRegistry.getOrmTable(CustomerDto.class)).thenReturn(custOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(custOrmTable);
        lenient().when(tableRegistry.getOrmTable(custTable)).thenReturn(custOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(custTable)).thenReturn(custOrmTable);

        when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        when(databaseProvider.executeUpdate(any(), eq(UpdateResult.class), any())).thenReturn(new UpdateResult(1));

        // When
        persistenceFacade.update(order);

        // Then
        verify(databaseProvider).executeUpdate(any(), eq(UpdateResult.class), any());
    }

    @Test
    void update_referencingUnpersistedDtoWithPkSet() throws SQLException {
        // Given
        final OrderDto order = new OrderDto();
        order.id = 1L;
        order.orderNo = "ORD-1";
        final CustomerDto customer = new CustomerDto();
        customer.id = 5L;
        customer.name = "Unpersisted Cust With PK";
        order.customer = customer;

        final Table custTable = new Table("", "public", "customers");
        final ColumnMetaData custIdCol = new ColumnMetaData(custTable, "ID", false, Types.NUMERIC);
        final ColumnMetaData custNameCol = new ColumnMetaData(custTable, "NAME", false, Types.VARCHAR);
        final TableMetaData custMeta = new TableMetaData(custTable, List.of("ID"), List.of(custIdCol, custNameCol));
        metaDataMap.put(custTable.qualifiedName(), custMeta);

        final OrmTable custOrmTable = new OrmTable(CustomerDto.class, custMeta, Map.of(
                changeTracker.classFieldAccessorCache().fieldAccessor(CustomerDto.class, "id"), custIdCol,
                changeTracker.classFieldAccessorCache().fieldAccessor(CustomerDto.class, "name"), custNameCol
        ), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));

        final Table orderTable = new Table("", "public", "orders");
        final ColumnMetaData orderIdCol = new ColumnMetaData(orderTable, "ID", false, Types.NUMERIC);
        final ColumnMetaData orderNoCol = new ColumnMetaData(orderTable, "ORDER_NO", false, Types.VARCHAR);
        final ColumnMetaData orderCustIdCol = new ColumnMetaData(orderTable, "CUST_ID", false, Types.NUMERIC);
        orderCustIdCol.setJoinColumnSupplier(() -> custIdCol);
        final TableMetaData orderMeta = new TableMetaData(orderTable, List.of("ID"), List.of(orderIdCol, orderNoCol, orderCustIdCol));
        metaDataMap.put(orderTable.qualifiedName(), orderMeta);

        final OrmTable orderOrmTable = new OrmTable(OrderDto.class, orderMeta, Map.of(
                changeTracker.classFieldAccessorCache().fieldAccessor(OrderDto.class, "id"), orderIdCol,
                changeTracker.classFieldAccessorCache().fieldAccessor(OrderDto.class, "orderNo"), orderNoCol,
                changeTracker.classFieldAccessorCache().fieldAccessor(OrderDto.class, "customer"), orderCustIdCol
        ), changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));

        order.customer = null;
        orderOrmTable.trackDto(order);
        order.customer = customer;
        order.orderNo = "ORD-2";

        lenient().when(tableRegistry.getOrmTable(OrderDto.class)).thenReturn(orderOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(OrderDto.class)).thenReturn(orderOrmTable);
        lenient().when(tableRegistry.getOrmTable(orderTable)).thenReturn(orderOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(orderTable)).thenReturn(orderOrmTable);

        lenient().when(tableRegistry.getOrmTable(CustomerDto.class)).thenReturn(custOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(custOrmTable);
        lenient().when(tableRegistry.getOrmTable(custTable)).thenReturn(custOrmTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(custTable)).thenReturn(custOrmTable);

        when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        lenient().when(databaseProvider.executeUpdate(any(), any(), any())).thenReturn(new UpdateResult(1));

        // When
        persistenceFacade.update(order);

        // Then
        verify(databaseProvider, times(2)).executeUpdate(any(), any(), any());
    }

    @Test
    void updateReverseMapping_withExistingMutableCollection() throws SQLException {
        // Given
        final CategoryDto category = new CategoryDto();
        category.id = 1L;
        category.name = "Electronics";
        category.products = new ArrayList<>();

        final ProductDto product = new ProductDto();
        product.name = "Tablet";

        final OrmTable categoryTable = createOrmTable(changeTracker, CategoryDto.class, "categories",
                Map.of("id", numeric("ID"), "name", varchar("NAME"), "products", new MappedOneToMany(
                        changeTracker.classFieldAccessorCache().fieldAccessor(ProductDto.class, "category"),
                        changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products")
                )), List.of("ID"));
        categoryTable.trackDto(category);

        final OrmTable productTable = createOrmTable(changeTracker, ProductDto.class, "products",
                Map.of("id", numeric("ID"), "name", varchar("NAME"), "category", numeric("CATEGORY_ID")), List.of("ID"));
        productTable.addOneToManyReverseMapping(changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products"));

        when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        when(databaseProvider.executeUpdate(any(), any(), any())).thenReturn(new InsertResult(1));

        // When
        persistenceFacade.save(product);

        // Then
        assertTrue(category.products.contains(product));
        verify(transactionManager).addRollbackCallback(any());
    }

    private PersistenceFacade createFacade(TableRegistry tableRegistry, TransactionalDatabaseProvider databaseProvider, ChangeTracker changeTracker, DtoConstructor dtoConstructor) {
        if (databaseProvider.metaData() == null) {
            final DatabaseProviderMetaData providerMetaData = new DatabaseProviderMetaData(true, DatabaseProviderMetaData.MergeCapability.USING_VALUES, DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW);
            when(databaseProvider.metaData()).thenReturn(providerMetaData);
        }

        if (databaseProvider.typeConverter() == null) {
            when(databaseProvider.typeConverter()).thenReturn(new DefaultTypeConverter());
        }

        if (databaseProvider.transactionManager() == null) {
            when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        }

        lenient().when(tableRegistry.getOrCreateSpiTable(anyString())).thenAnswer(invocation -> {
            String tableName = invocation.getArgument(0);
            if (tableName.contains(".")) {
                tableName = tableName.substring(tableName.lastIndexOf('.') + 1);
            }

            return new Table("", "public", tableName);
        });

        try {
            lenient().when(databaseProvider.tableMetaData(any(), any())).thenAnswer(invocation -> {
                final Table table = invocation.getArgument(0);
                return metaDataMap.get(table.qualifiedName());
            });

            lenient().when(databaseProvider.toSql(any(), any())).thenAnswer(invocation -> {
                final org.litebridge.db.spi.Operation op = invocation.getArgument(0);
                final String tableName = op.table() instanceof Table t ? t.name() : "";
                return "INSERT INTO " + tableName + (tableName.equals("customers") ? " WHERE id IS NULL" : "");
            });
        } catch (SQLException e) {
            // Should not happen
        }

        final SqlFunctionRegistry sqlFunctionRegistry = new SqlFunctionRegistryFactory(new LabelGenerator(), mock(SelectSqlGenerator.class)).create();
        when(databaseProvider.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);

        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, databaseProvider.transactionManager());
        final LitebridgeConfig litebridgeConfig = new LitebridgeConfig();

        final LitebridgeContext litebridgeContext = new LitebridgeContext(LitebridgeContext.Mode.DTO,
                litebridgeConfig,
                databaseProvider,
                new QueryPlanCache(),
                new NoOpAliasGenerator(),
                tableRegistry,
                tableMetaDataCache,
                new ClassFieldAccessorCache(MethodHandles.lookup()),
                transactionManager,
                new SelectEngine(dtoConstructor)
        );

        return new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor, litebridgeContext);
    }

    private OrmTable createOrmTable(ChangeTracker changeTracker, Class<?> dtoClass, String tableName, Map<String, Object> fieldToTarget, List<String> pkColumns) {
        return createOrmTable(changeTracker, dtoClass, tableName, fieldToTarget, pkColumns, new java.util.HashSet<>(pkColumns));
    }

    private OrmTable createOrmTable(ChangeTracker changeTracker, Class<?> dtoClass, String tableName, Map<String, Object> fieldToTarget, List<String> pkColumns, Set<String> autoIncColumns) {
        final Table table = new Table("", "public", tableName);
        final List<ColumnMetaData> columns = fieldToTarget.entrySet().stream()
                .filter(e -> e.getValue() instanceof TestCol)
                .map(e -> {
                    TestCol tc = (TestCol) e.getValue();
                    final boolean autoInc = autoIncColumns.contains(tc.name());
                    return new ColumnMetaData(table, tc.name(), !pkColumns.contains(tc.name()), tc.type(), 0, 0, autoInc, null, null);
                })
                .toList();
        final TableMetaData tableMetaData = new TableMetaData(table, pkColumns, columns);
        metaDataMap.put(table.qualifiedName(), tableMetaData);

        final Map<FieldAccessor, MappedFieldTarget> fieldTargetMap = new HashMap<>();
        fieldToTarget.forEach((field, target) -> {
            final FieldAccessor accessor;

            if (Proxy.isProxyClass(dtoClass)) {
                accessor = new NoOpFieldAccessor();
            } else {
                accessor = changeTracker.classFieldAccessorCache().fieldAccessor(dtoClass, field);
            }

            if (target instanceof TestCol col) {
                fieldTargetMap.put(accessor, tableMetaData.column(col.name()));
            } else if (target instanceof org.litebridge.db.spi.MappedFieldTarget mft) {
                fieldTargetMap.put(accessor, mft);
            }
        });

        final OrmTable ormTable = new OrmTable(dtoClass, tableMetaData, fieldTargetMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
        lenient().when(tableRegistry.getOrmTable(dtoClass)).thenReturn(ormTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(dtoClass)).thenReturn(ormTable);
        lenient().when(tableRegistry.getOrmTable(table)).thenReturn(ormTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(table)).thenReturn(ormTable);
        lenient().when(tableRegistry.getOrmTable(table.name())).thenReturn(ormTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(table.name())).thenReturn(ormTable);
        lenient().when(tableRegistry.getOrmTable(table.qualifiedName())).thenReturn(ormTable);
        lenient().when(tableRegistry.getOrmTableOrThrow(table.qualifiedName())).thenReturn(ormTable);
        return ormTable;
    }

    private static TestCol varchar(String name) {
        return new TestCol(name, Types.VARCHAR);
    }

    private static TestCol numeric(String name) {
        return new TestCol(name, Types.NUMERIC);
    }

    public static class CustomerDto {
        private Long id;
        private String name;
    }

    public static class OrderDto {
        private Long id;
        private String orderNo;
        private CustomerDto customer;
    }

    public static class ProductDto {
        private Long id;
        private String name;
        private ProductDto relatedProduct;
        private CategoryDto category;
        private List<TagDto> tags;
    }

    public static class CategoryDto {
        private Long id;
        private String name;
        private List<ProductDto> products;
    }

    public static class TagDto {
        private Long id;
        private String name;
    }

    public record PersonRecord(Long id, String name) {
    }

    public record DepartmentRecord(Long id, String name, List<EmployeeDto> employees) {
    }

    public static class EmployeeDto {
        private Long id;
        private String name;
        private DepartmentRecord department;
    }

    public static class CompositeChildDto {
        private Long keyPartA;
        private String keyPartB;
        private String detail;
    }

    public static class CompositeParentEntity {
        private Long id;
        private CompositeChildDto child;
    }

    public static class CompositePkEntity {
        private CompositeChildDto pk;
        private String name;
    }

    private record TestCol(String name, int type) {
    }
}
