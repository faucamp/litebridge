package org.litebridgedb.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridgedb.convert.DefaultTypeConverter;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.tx.TransactionManager;
import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.tracking.ChangeTracker;
import org.litebridgedb.tracking.ClassFieldAccessorCache;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

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

    private static OrmTable createOrmTable(ChangeTracker changeTracker, Class<?> dtoClass, String tableName, Map<String, TestCol> fieldToColumn, List<String> pkColumns) {
        final org.litebridgedb.db.spi.Table table = new org.litebridgedb.db.spi.Table("", "public", tableName);
        final List<ColumnMetaData> columns = fieldToColumn.entrySet().stream()
                .map(e -> new ColumnMetaData(table, e.getValue().name(), false, e.getValue().type()))
                .toList();
        final TableMetaData tableMetaData = new TableMetaData(table, pkColumns, columns);

        final Map<org.litebridgedb.tracking.FieldAccessor, org.litebridgedb.db.spi.MappedFieldTarget> fieldTargetMap = new java.util.HashMap<>();
        fieldToColumn.forEach((field, column) -> {
            fieldTargetMap.put(changeTracker.classFieldAccessorCache().fieldAccessor(dtoClass, field), tableMetaData.column(column.name()));
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

    private record TestCol(String name, int type) {
    }
}
