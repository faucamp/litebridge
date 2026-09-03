package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateColumnExpression;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.DelegateExpressionFactory;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SelectReference;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.expression.SubselectExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.function.SelectColumn;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.engine.SelectEngine;
import org.litebridge.orm.engine.compiler.QueryCompiler;
import org.litebridge.orm.expression.TestColumnExpression;
import org.litebridge.orm.expression.TestColumnExpressionFactory;
import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;
import org.litebridge.orm.persistence.manytomany.NoOpFieldAccessor;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.ClassFieldAccessorCache;

import java.lang.invoke.MethodHandles;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersistenceFacadeTest {

    private final Map<String, TableMetaData> metaDataMap = new HashMap<>();

    private PersistenceFacade createFacade(TableRegistry tableRegistry, TransactionalDatabaseProvider databaseProvider, ChangeTracker changeTracker, DtoConstructor dtoConstructor) {
        final TransactionManager transactionManager = mock(TransactionManager.class);

        if (databaseProvider.getTypeConverter() == null) {
            when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        }

        if (databaseProvider.transactionManager() == null) {
            when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        }

        when(tableRegistry.getOrCreateSpiTable(anyString())).thenAnswer(invocation -> {
            String tableName = invocation.getArgument(0);
            if (tableName.contains(".")) {
                tableName = tableName.substring(tableName.lastIndexOf('.') + 1);
            }

            return new Table("", "public", tableName);
        });

        try {
            when(databaseProvider.tableMetaData(any(), any())).thenAnswer(invocation -> {
                org.litebridge.db.spi.Table table = invocation.getArgument(0);
                return metaDataMap.get(table.qualifiedName());
            });
            when(databaseProvider.toSql(any(), any())).thenAnswer(invocation -> {
                org.litebridge.db.spi.Operation op = invocation.getArgument(0);
                return "INSERT INTO " + op.table().name() + (op.table().name().equals("customers") ? " WHERE id IS NULL" : "");
            });
        } catch (SQLException e) {
            // Should not happen
        }

        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn((column, args) -> new TestColumnExpression(column));
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        when(selectRegistry.reference()).thenReturn(column -> new SelectReference(column) {
            @Override
            public String toSql(org.litebridge.db.spi.Operation operation, ClauseType context, @Nullable DelegateExpression parent) {
                return column().name();
            }
        });
        when(selectRegistry.subselect()).thenReturn(select -> mock(SubselectExpression.class));

        final SqlFunctionRegistry.Aggregate aggregateRegistry = mock(SqlFunctionRegistry.Aggregate.class);
        when(sqlFunctionRegistry.aggregate()).thenReturn(aggregateRegistry);
        final DelegateExpressionFactory delegateFactory = (target, args) -> mock(DelegateColumnExpression.class);
        when(aggregateRegistry.avg()).thenReturn(delegateFactory);
        when(aggregateRegistry.min()).thenReturn(delegateFactory);
        when(aggregateRegistry.max()).thenReturn(delegateFactory);
        when(aggregateRegistry.count()).thenReturn(mock(SelectExpression.class));

        final SqlFunctionRegistry.Scalar scalarRegistry = mock(SqlFunctionRegistry.Scalar.class);
        when(sqlFunctionRegistry.scalar()).thenReturn(scalarRegistry);
        when(scalarRegistry.upper()).thenReturn(delegateFactory);
        when(scalarRegistry.lower()).thenReturn(delegateFactory);
        when(scalarRegistry.substring()).thenReturn(delegateFactory);
        when(scalarRegistry.abs()).thenReturn(delegateFactory);

        final SqlFunctionRegistry.Date dateRegistry = mock(SqlFunctionRegistry.Date.class);
        when(sqlFunctionRegistry.date()).thenReturn(dateRegistry);
        when(dateRegistry.currentTimestamp()).thenReturn(mock(SelectExpression.class));

        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);

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

    @Test
    void insert() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.name = "test";
        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        when(databaseProvider.insert(any(), any())).thenReturn(new InsertResult(1));

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
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "new name";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        table.trackDto(dto);
        // Simulate change
        dto.name = "changed";

        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
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
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
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
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto c1 = new CustomerDto();
        c1.name = "c1";
        final CustomerDto c2 = new CustomerDto();
        c2.name = "c2";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        when(databaseProvider.insert(any(), any())).thenReturn(new InsertResult(1));

        // When
        facade.save(List.of(c1, c2));

        // Then
        verify(databaseProvider, org.mockito.Mockito.times(2)).insert(any(), any());
    }

    @Test
    void save_cycle_doesNotThrow() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final ProductDto p1 = new ProductDto();
        p1.name = "p1";
        final ProductDto p2 = new ProductDto();
        p2.name = "p2";
        // Circular dependency
        p1.relatedProduct = p2;
        p2.relatedProduct = p1;

        final OrmTable table = createOrmTable(changeTracker, ProductDto.class, "products", Map.of("id", numeric("ID"), "name", varchar("NAME"), "relatedProduct", numeric("RELATED_ID")), List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(ProductDto.class)).thenReturn(table);
        when(databaseProvider.insert(any(), any())).thenReturn(new InsertResult(1));

        // When
        facade.save(p1);

        // Then
        verify(databaseProvider, org.mockito.Mockito.atLeastOnce()).insert(any(), any());
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
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final PersonRecord person = new PersonRecord(null, "John");

        final OrmTable table = createOrmTable(changeTracker, PersonRecord.class, "persons", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(PersonRecord.class)).thenReturn(table);
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
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "test";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        table.syncPersistedDto(dto); // Marks as persisted and takes snapshot

        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
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
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.name = "test";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
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
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto customer = new CustomerDto();
        customer.name = "cust";

        final OrderDto order = new OrderDto();
        order.orderNo = "ORD1";
        order.customer = customer;

        final OrmTable customerTable = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        final OrmTable orderTable = createOrmTable(changeTracker, OrderDto.class, "orders", Map.of("id", numeric("ID"), "orderNo", varchar("ORDER_NO"), "customer", numeric("CUST_ID")), List.of("ID"));

        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(customerTable);
        when(tableRegistry.getOrmTableOrThrow(OrderDto.class)).thenReturn(orderTable);

        when(databaseProvider.insert(any(), any())).thenAnswer(invocation -> {
            final PreparedSql preparedSql = invocation.getArgument(0);

            if (preparedSql.sql().contains("customers")) {
                return new InsertResult(1, Map.of(customerTable.getMetaData().column("ID"), 1L));
            }
            return new InsertResult(1);
        });

        // When
        facade.save(order);

        // Then
        verify(databaseProvider).insert(argThat(i -> i.sql().contains("customers")), any());
        verify(databaseProvider).insert(argThat(i -> i.sql().contains("orders")), any());
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
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CategoryDto category = new CategoryDto();
        category.name = "cat";
        final ProductDto product = new ProductDto();
        product.name = "prod";
        category.products = new ArrayList<>(List.of(product));
        product.category = category;

        final OrmTable categoryTable = createOrmTable(changeTracker, CategoryDto.class, "categories", Map.of("id", numeric("ID"), "name", varchar("NAME"), "products", new MappedOneToMany(null, changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products"))), List.of("ID"));
        final OrmTable productTable = createOrmTable(changeTracker, ProductDto.class, "products", Map.of("id", numeric("ID"), "name", varchar("NAME"), "category", numeric("CAT_ID")), List.of("ID"));

        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        when(tableRegistry.getOrmTableOrThrow(CategoryDto.class)).thenReturn(categoryTable);
        when(tableRegistry.getOrmTableOrThrow(ProductDto.class)).thenReturn(productTable);

        when(databaseProvider.insert(any(), any())).thenAnswer(invocation -> {
            final PreparedSql preparedSql = invocation.getArgument(0);

            if (preparedSql.sql().contains("categories")) {
                return new InsertResult(1, Map.of(categoryTable.getMetaData().column("ID"), 1L));
            }
            return new InsertResult(1);
        });

        // When
        facade.save(category);

        // Then
        verify(databaseProvider).insert(argThat(i -> i.sql().contains("categories")), any());
        verify(databaseProvider).insert(argThat(i -> i.sql().contains("products")), any());
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
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final ProductDto product = new ProductDto();
        product.id = 1L;
        product.name = "prod";
        final TagDto tag = new TagDto();
        tag.id = 1L;
        tag.name = "tag";
        product.tags = new ArrayList<>(List.of(tag));

        final OrmTable productTable = createOrmTable(changeTracker, ProductDto.class, "products", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        productTable.syncPersistedDto(product);

        final OrmTable joinTable = createOrmTable(changeTracker, ProductTag.class, "product_tags", Map.of("prod_id", numeric("PROD_ID"), "tag_id", numeric("TAG_ID")), List.of());
        when(tableRegistry.getOrmTableOrThrow(ProductTag.class)).thenReturn(joinTable);
        final MappedManyToMany m2m = new MappedManyToMany(joinTable, "prod_id", changeTracker.classFieldAccessorCache().fieldAccessor(ProductDto.class, "tags"), null, "tag_id");

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
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

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
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.id = null;

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID")), List.of("ID"));
        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.delete(any(), any())).thenReturn(new UpdateResult(1));

        // When
        facade.delete(dto);

        // Then
        verify(databaseProvider).delete(argThat(po -> po.sql().contains("IS NULL")), any());
    }

    @Test
    void update_noChanges() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "test";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"));
        table.syncPersistedDto(dto);

        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
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
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();
        dto.id = 1L;
        dto.name = "test";

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", Map.of("id", numeric("ID"), "name", varchar("NAME")), List.of("ID"), Collections.emptySet());
        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        when(databaseProvider.insert(any(), any())).thenReturn(new InsertResult(1));

        // When
        facade.insert(dto);

        // Then
        verify(databaseProvider).insert(argThat(i -> i.updateMetaData() != null && !i.updateMetaData().returnGeneratedKeys()), any());
    }

    @Test
    void save_withNoOpFieldAccessor() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CustomerDto dto = new CustomerDto();

        final Map<String, Object> fields = new HashMap<>();
        fields.put("id", numeric("ID"));
        fields.put("name", new NoOpFieldAccessor());

        final OrmTable table = createOrmTable(changeTracker, CustomerDto.class, "customers", fields, List.of("ID"));
        table.trackDto(dto);
        dto.id = 1L;
        dto.name = "test";

        when(tableRegistry.getOrmTableOrThrow(CustomerDto.class)).thenReturn(table);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        when(databaseProvider.insert(any(), any())).thenReturn(new InsertResult(1));

        // When
        facade.save(dto);

        // Then
        verify(databaseProvider).insert(argThat(i -> i.bindValues().size() == 1), any());
    }

    @Test
    void save_withDeeplyNestedGeneratedKeys() throws SQLException {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        when(databaseProvider.transactionManager()).thenReturn(mock(TransactionManager.class));
        when(databaseProvider.getTypeConverter()).thenReturn(new DefaultTypeConverter());
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
        final PersistenceFacade facade = createFacade(tableRegistry, databaseProvider, changeTracker, dtoConstructor);

        final CategoryDto category = new CategoryDto();
        category.name = "cat";
        final ProductDto product = new ProductDto();
        product.name = "prod";
        category.products = new ArrayList<>(List.of(product));
        product.category = category;

        final OrmTable categoryTable = createOrmTable(changeTracker, CategoryDto.class, "categories", Map.of("id", numeric("ID"), "name", varchar("NAME"), "products", new MappedOneToMany(null, changeTracker.classFieldAccessorCache().fieldAccessor(CategoryDto.class, "products"))), List.of("ID"));
        final OrmTable productTable = createOrmTable(changeTracker, ProductDto.class, "products", Map.of("id", numeric("ID"), "name", varchar("NAME"), "category", numeric("CAT_ID")), List.of("ID"));

        when(tableRegistry.getOrmTableOrThrow(CategoryDto.class)).thenReturn(categoryTable);
        when(tableRegistry.getOrmTableOrThrow(ProductDto.class)).thenReturn(productTable);

        when(databaseProvider.insert(any(), any())).thenAnswer(invocation -> {
            final PreparedSql preparedSql = invocation.getArgument(0);
            if (preparedSql.sql().contains("categories")) {
                return new InsertResult(1, Map.of(categoryTable.getMetaData().column("ID"), 10L));
            } else if (preparedSql.sql().contains("products")) {
                return new InsertResult(1, Map.of(productTable.getMetaData().column("ID"), 20L));
            }
            return new InsertResult(1);
        });

        // When
        facade.save(category);

        // Then
        assertEquals(10L, category.id);
        assertEquals(20L, product.id);
    }

    private void setupMockSqlFunctions(TransactionalDatabaseProvider databaseProvider) {
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        when(databaseProvider.getSqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
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

        final Map<org.litebridge.tracking.FieldAccessor, org.litebridge.db.spi.MappedFieldTarget> fieldTargetMap = new java.util.HashMap<>();
        fieldToTarget.forEach((field, target) -> {
            final org.litebridge.tracking.FieldAccessor accessor = changeTracker.classFieldAccessorCache().fieldAccessor(dtoClass, field);
            if (target instanceof TestCol col) {
                fieldTargetMap.put(accessor, tableMetaData.column(col.name()));
            } else if (target instanceof org.litebridge.db.spi.MappedFieldTarget mft) {
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

    public record PersonRecord(Long id, String name) {
    }

    private record TestCol(String name, int type) {
    }
}
