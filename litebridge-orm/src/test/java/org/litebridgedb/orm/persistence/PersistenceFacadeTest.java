package org.litebridgedb.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridgedb.convert.DefaultTypeConverter;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.expression.LiteralExpression;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.SelectColumn;
import org.litebridgedb.db.spi.tx.TransactionManager;
import org.litebridgedb.db.spi.update.InsertResult;
import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.orm.expression.TestColumnExpressionFactory;
import org.litebridgedb.tracking.ChangeTracker;
import org.litebridgedb.tracking.ClassFieldAccessorCache;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersistenceFacadeTest {

    @Test
    void insert() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.name = "test";
        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.insert(any(), any())).thenReturn(new org.litebridgedb.db.spi.update.InsertResult(1));

        // When
        facade.insert(dto);

        // Then
        verify(databaseProvider).insert(any(), any());
    }

    @Test
    void update() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn((column, args) -> new SelectColumn(column, mock(ColumnIdentifierGenerator.class)));
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "new name";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        table.trackDto(dto);
        // Simulate change
        dto.name = "changed";

        when(tableRegistry.getTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.update(any(), any())).thenReturn(new UpdateResult(1));

        // When
        facade.update(dto);

        // Then
        verify(databaseProvider).update(any(), any());
    }

    @Test
    void delete() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.delete(any(), any())).thenReturn(new UpdateResult(1));

        // When
        facade.delete(dto);

        // Then
        verify(databaseProvider).delete(any(), any());
    }

    @Test
    void save_collection() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto c1 = new CustomerDto();
        c1.name = "c1";
        final CustomerDto c2 = new CustomerDto();
        c2.name = "c2";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.insert(any(), any())).thenReturn(new InsertResult(1));

        // When
        facade.save(List.of(c1, c2));

        // Then
        verify(databaseProvider, org.mockito.Mockito.times(2)).insert(any(), any());
    }

    @Test
    void save_cycle_throwsException() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final ProductDto p1 = new ProductDto();
        p1.name = "p1";
        final ProductDto p2 = new ProductDto();
        p2.name = "p2";
        // Circular dependency
        p1.relatedProduct = p2;
        p2.relatedProduct = p1;

        final OrmTable table = createOrmTable(changeTracker, ProductDto.class, "products", Map.of("id", numeric("ID"), "name", varchar("NAME"), "relatedProduct", numeric("RELATED_ID")), List.of("ID"));
        when(tableRegistry.getTableOrThrow(ProductDto.class)).thenReturn(table);
        when(databaseProvider.insert(any(), any())).thenReturn(new InsertResult(1));

        // When / Then
        assertThrows(IllegalStateException.class, () -> facade.save(p1));
    }

    @Test
    void save_record() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final PersonRecord person = new PersonRecord(null, "John");

        final OrmTable table = createOrmTable(changeTracker, PersonRecord.class, "persons", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getTableOrThrow(PersonRecord.class)).thenReturn(table);
        when(databaseProvider.insert(any(), any())).thenReturn(new InsertResult(1, Map.of(table.getMetaData().column("ID"), 123L)));

        // When
        facade.save(person);

        // Then
        verify(databaseProvider).insert(any(), any());
    }

    @Test
    void save_noChanges() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "test";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        table.syncPersistedDto(dto); // Marks as persisted and takes snapshot

        when(tableRegistry.getTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        setupMockSqlFunctions(databaseProvider);

        // When
        facade.save(dto);

        // Then
        verify(databaseProvider, org.mockito.Mockito.never()).update(any(), any());
    }

    @Test
    void updateDtoPrimaryKey_rollback() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TransactionManager transactionManager = mock(TransactionManager.class);
        when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        setupMockSqlFunctions(databaseProvider);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.name = "test";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.insert(any(), any())).thenReturn(new InsertResult(1, Map.of(table.getMetaData().column("ID"), 1L)));

        final List<Runnable> rollbackCallbacks = new ArrayList<>();
        org.mockito.Mockito.doAnswer(invocation -> {
            rollbackCallbacks.add(invocation.getArgument(0));
            return null;
        }).when(transactionManager).addRollbackCallback(any());

        // When
        facade.save(dto);
        assertEquals(1L, dto.id);

        // Simulate rollback
        rollbackCallbacks.forEach(Runnable::run);

        // Then
        assertNull(dto.id);
    }

    @Test
    void save_withManyToOne() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto customer = new CustomerDto();
        customer.name = "cust";

        final OrderDto order = new OrderDto();
        order.orderNo = "ORD1";
        order.customer = customer;

        final OrmTable customerTable = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        final OrmTable orderTable = createOrmTable(changeTracker, OrderDto.class, "orders", Map.of("id", numeric("ID"), "orderNo", varchar("ORDER_NO"), "customer", numeric("CUST_ID")), List.of("ID"));

        when(tableRegistry.getTableOrThrow(CustomerDto.class)).thenReturn(customerTable);
        when(tableRegistry.getTableOrThrow(OrderDto.class)).thenReturn(orderTable);

        when(databaseProvider.insert(any(), any())).thenAnswer(invocation -> {
            org.litebridgedb.db.spi.update.Insert insert = invocation.getArgument(0);
            if (insert.table().name().equals("customers")) {
                return new org.litebridgedb.db.spi.update.InsertResult(1, Map.of(customerTable.getMetaData().column("ID"), 1L));
            }
            return new org.litebridgedb.db.spi.update.InsertResult(1);
        });

        // When
        facade.save(order);

        // Then
        verify(databaseProvider).insert(argThat(i -> i.table().name().equals("customers")), any());
        verify(databaseProvider).insert(argThat(i -> i.table().name().equals("orders")), any());
    }

    @Test
    void save_withOneToMany() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CategoryDto category = new CategoryDto();
        category.name = "cat";
        final ProductDto product = new ProductDto();
        product.name = "prod";
        category.products = new ArrayList<>(List.of(product));
        product.category = category;

        final OrmTable categoryTable = createOrmTable(changeTracker, CategoryDto.class, "categories", Map.of("id", numeric("ID"), "name", varchar("NAME"), "products", new MappedOneToMany(null, changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products"))), List.of("ID"));
        final OrmTable productTable = createOrmTable(changeTracker, ProductDto.class, "products", Map.of("id", numeric("ID"), "name", varchar("NAME"), "category", numeric("CAT_ID")), List.of("ID"));

        when(tableRegistry.getTableOrThrow(CategoryDto.class)).thenReturn(categoryTable);
        when(tableRegistry.getTableOrThrow(ProductDto.class)).thenReturn(productTable);

        when(databaseProvider.insert(any(), any())).thenAnswer(invocation -> {
            org.litebridgedb.db.spi.update.Insert insert = invocation.getArgument(0);
            if (insert.table().name().equals("categories")) {
                return new org.litebridgedb.db.spi.update.InsertResult(1, Map.of(categoryTable.getMetaData().column("ID"), 1L));
            }
            return new org.litebridgedb.db.spi.update.InsertResult(1);
        });

        // When
        facade.save(category);

        // Then
        verify(databaseProvider).insert(argThat(i -> i.table().name().equals("categories")), any());
        verify(databaseProvider).insert(argThat(i -> i.table().name().equals("products")), any());
    }

    @Test
    void save_withManyToMany() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final ProductDto product = new ProductDto();
        product.id = 1L;
        product.name = "prod";
        final TagDto tag = new TagDto();
        tag.name = "tag";
        product.tags = new ArrayList<>(List.of(tag));

        final OrmTable productTable = createOrmTable(changeTracker, ProductDto.class, "products", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        productTable.syncPersistedDto(product);

        final OrmTable joinTable = createOrmTable(changeTracker, ProductTag.class, "product_tags", Map.of("prod_id", numeric("PROD_ID"), "tag_id", numeric("TAG_ID")), List.of());
        final MappedManyToMany m2m = new MappedManyToMany(joinTable, "PROD_ID", changeTracker.classFieldAccessorCache().fieldAccessor(ProductDto.class, "tags"), null, "TAG_ID");

        final Map<String, Object> productFields = new HashMap<>();
        productFields.put("id", numeric("ID"));
        productFields.put("name", varchar("NAME"));
        productFields.put("tags", m2m);
        final OrmTable productTableWithM2M = createOrmTable(changeTracker, ProductDto.class, "products", productFields, List.of("ID"));
        productTableWithM2M.syncPersistedDto(product);
        productTableWithM2M.trackDto(product);

        // Trigger change in collection
        product.tags.add(new TagDto()); // This will be the second tag, but for simplicity let's just say we added one

        final OrmTable tagTable = createOrmTable(changeTracker, TagDto.class, "tags", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));

        when(tableRegistry.getTableOrThrow(ProductDto.class)).thenReturn(productTableWithM2M);
        when(tableRegistry.getTableOrThrow(TagDto.class)).thenReturn(tagTable);

        when(databaseProvider.insert(any(), any())).thenReturn(new InsertResult(1));
        setupMockSqlFunctions(databaseProvider);

        // When
        facade.save(product);

        // Then
        verify(databaseProvider, org.mockito.Mockito.atLeastOnce()).insert(any(), any());
    }

    @Test
    void updateOneToManyReverseMappings() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CategoryDto category = new CategoryDto();
        category.name = "cat";
        changeTracker.trackDto(category);

        final ProductDto product = new ProductDto();
        product.name = "prod";
        product.category = category;

        final OrmTable categoryTable = createOrmTable(changeTracker, CategoryDto.class, "categories", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        final OrmTable productTable = createOrmTable(changeTracker, ProductDto.class, "products", Map.of("id", numeric("ID"), "name", varchar("NAME"), "category", numeric("CAT_ID")), List.of("ID"));
        productTable.addOneToManyReverseMapping(changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products"));

        when(tableRegistry.getTableOrThrow(CategoryDto.class)).thenReturn(categoryTable);
        when(tableRegistry.getTableOrThrow(ProductDto.class)).thenReturn(productTable);
        when(databaseProvider.insert(any(), any())).thenReturn(new InsertResult(1));

        // When
        facade.save(product);

        // Then
        assertNotNull(category.products);
        assertTrue(category.products.contains(product));
    }

    @Test
    void delete_withNullPk() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        setupMockSqlFunctions(databaseProvider);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.id = null;

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID")), List.of("ID"));
        when(tableRegistry.getTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.delete(any(), any())).thenReturn(new UpdateResult(1));

        // When
        facade.delete(dto);

        // Then
        verify(databaseProvider).delete(argThat(d -> {
            return d.where().conditions().get(0).condition().operator() == org.litebridgedb.db.spi.query.Operator.IS_NULL;
        }), any());
    }

    @Test
    void update_noChanges() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "test";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        table.syncPersistedDto(dto);

        when(tableRegistry.getTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        setupMockSqlFunctions(databaseProvider);

        // When
        facade.update(dto);

        // Then
        verify(databaseProvider, org.mockito.Mockito.never()).update(any(), any());
    }

    @Test
    void insert_withExplicitPk() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "test";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"), Set.of("ID"));
        when(tableRegistry.getTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.insert(any(), any())).thenReturn(new InsertResult(1));

        // When
        facade.insert(dto);

        // Then
        verify(databaseProvider).insert(argThat(i -> !((org.litebridgedb.db.spi.update.Insert)i).returnGeneratedKeys()), any());
    }

    @Test
    void save_withNoOpFieldAccessor() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = new PersistenceFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();

        final Map<String, Object> fields = new HashMap<>();
        fields.put("id", numeric("ID"));
        fields.put("name", new org.litebridgedb.orm.persistence.manytomany.NoOpFieldAccessor());

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", fields, List.of("ID"));
        table.trackDto(dto);
        dto.id = 1L;
        dto.name = "test";

        when(tableRegistry.getTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.insert(any(), any())).thenReturn(new InsertResult(1));

        // When
        facade.save(dto);

        // Then
        verify(databaseProvider).insert(argThat(i -> i.rows().get(0).columns().size() == 1), any());
    }

    private void setupMockSqlFunctions(TransactionalDatabaseProvider databaseProvider) {
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
    }

    private static OrmTable createOrmTable(ChangeTracker changeTracker, Class<?> dtoClass, String tableName, Map<String, Object> fieldToTarget, List<String> pkColumns) {
        return createOrmTable(changeTracker, dtoClass, tableName, fieldToTarget, pkColumns, Collections.emptySet());
    }

    private static OrmTable createOrmTable(ChangeTracker changeTracker, Class<?> dtoClass, String tableName, Map<String, Object> fieldToTarget, List<String> pkColumns, Set<String> autoIncColumns) {
        final org.litebridgedb.db.spi.Table table = new org.litebridgedb.db.spi.Table("", "public", tableName);
        final List<ColumnMetaData> columns = fieldToTarget.entrySet().stream()
                .filter(e -> e.getValue() instanceof TestCol)
                .map(e -> {
                    TestCol tc = (TestCol) e.getValue();
                    return new ColumnMetaData(table, tc.name(), autoIncColumns.contains(tc.name()), tc.type());
                })
                .toList();
        final TableMetaData tableMetaData = new TableMetaData(table, pkColumns, columns);

        final Map<org.litebridgedb.tracking.FieldAccessor, org.litebridgedb.db.spi.MappedFieldTarget> fieldTargetMap = new java.util.HashMap<>();
        fieldToTarget.forEach((field, target) -> {
            final org.litebridgedb.tracking.FieldAccessor accessor = changeTracker.classFieldAccessorCache().fieldAccessor(dtoClass, field);
            if (target instanceof TestCol col) {
                fieldTargetMap.put(accessor, tableMetaData.column(col.name()));
            } else if (target instanceof org.litebridgedb.db.spi.MappedFieldTarget mft) {
                fieldTargetMap.put(accessor, mft);
            }
        });

        return new OrmTable(dtoClass, tableMetaData, fieldTargetMap, changeTracker, new ClassFieldAccessorCache(MethodHandles.lookup()));
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

    public static class ProductTag {
        private Long prod_id;
        private Long tag_id;
    }

    public record PersonRecord(Long id, String name) {}

    private record TestCol(String name, int type) {
    }
}
