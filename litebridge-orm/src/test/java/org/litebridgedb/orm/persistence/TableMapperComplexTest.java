package org.litebridgedb.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.orm.api.register.DtoTableSpecBuilder;
import org.litebridgedb.orm.api.register.RegistrationContext;
import org.litebridgedb.orm.api.spec.DtoTableSpec;
import org.litebridgedb.orm.api.spec.TableSpec;
import org.litebridgedb.tracking.ChangeTracker;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TableMapperComplexTest {

    @Test
    void mapToTable_withOneToMany() throws SQLException {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        TableRegistry tableRegistry = mock(TableRegistry.class);
        ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        // Register Order first
        org.litebridgedb.db.spi.Table orderTable = new org.litebridgedb.db.spi.Table("", "public", "orders");
        ColumnMetaData orderIdColumn = new ColumnMetaData(orderTable, "ID", false, Types.BIGINT);
        TableMetaData orderMeta = new TableMetaData(orderTable, List.of("ID"), List.of(orderIdColumn));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(orderMeta);
        
        // When
        RegistrationContext context = new RegistrationContext();
        DtoTableSpec orderSpec = ((DtoTableSpecBuilder) context.mapToTable("orders")
                .mapField("id").toColumn("ID"))
                .buildDtoTableSpec(OrderDto.class);
        
        TableMapper.MappedTable mappedOrder = mapper.mapToTable(MethodHandles.lookup(), OrderDto.class, orderSpec.tableSpec());
        when(tableRegistry.getTable(OrderDto.class)).thenReturn(mappedOrder.ormTable());

        // Now register Customer with OneToMany to Order
        org.litebridgedb.db.spi.Table customerTable = new org.litebridgedb.db.spi.Table("", "public", "customers");
        ColumnMetaData custIdColumn = new ColumnMetaData(customerTable, "ID", false, Types.BIGINT);
        TableMetaData custMeta = new TableMetaData(customerTable, List.of("ID"), List.of(custIdColumn));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(custMeta);

        DtoTableSpec custSpec = ((DtoTableSpecBuilder) new RegistrationContext().mapToTable("customers")
                .mapField("id").toColumn("ID")
                .mapField("orders").oneToMany(b -> b.mappedByField("customer")))
                .buildDtoTableSpec(CustomerDto.class);

        // When
        TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), CustomerDto.class, custSpec.tableSpec());

        // Then
        assertNotNull(result);
    }

    @Test
    void mapToTable_invalidDto() {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        TableRegistry tableRegistry = mock(TableRegistry.class);
        ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), String.class, mock(TableSpec.class)));
    }

    private static class CustomerDto {
        private Long id;
        private List<OrderDto> orders;
    }

    private static class OrderDto {
        private Long id;
        private CustomerDto customer;
    }
    @Test
    void mapToTable_withManyToMany() throws SQLException {
        // Given
        TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        TableRegistry tableRegistry = mock(TableRegistry.class);
        ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        // Customer
        org.litebridgedb.db.spi.Table customerTable = new org.litebridgedb.db.spi.Table("", "public", "customers");
        ColumnMetaData custIdColumn = new ColumnMetaData(customerTable, "ID", false, Types.BIGINT);
        TableMetaData custMeta = new TableMetaData(customerTable, List.of("ID"), List.of(custIdColumn));
        
        // Tag
        org.litebridgedb.db.spi.Table tagTable = new org.litebridgedb.db.spi.Table("", "public", "tags");
        ColumnMetaData tagIdColumn = new ColumnMetaData(tagTable, "ID", false, Types.BIGINT);
        TableMetaData tagMeta = new TableMetaData(tagTable, List.of("ID"), List.of(tagIdColumn));

        // Join Table
        org.litebridgedb.db.spi.Table joinTable = new org.litebridgedb.db.spi.Table("", "public", "customer_tags");
        ColumnMetaData custJoinCol = new ColumnMetaData(joinTable, "CUST_ID", false, Types.BIGINT);
        ColumnMetaData tagJoinCol = new ColumnMetaData(joinTable, "TAG_ID", false, Types.BIGINT);
        TableMetaData joinMeta = new TableMetaData(joinTable, List.of("CUST_ID", "TAG_ID"), List.of(custJoinCol, tagJoinCol));

        when(databaseProvider.tableMetaData(any(), any())).thenAnswer(invocation -> {
            TableSpec spec = invocation.getArgument(0);
            if (spec.name().equals("customers")) return custMeta;
            if (spec.name().equals("tags")) return tagMeta;
            if (spec.name().equals("customer_tags")) return joinMeta;
            return null;
        });

        // Register Tag
        DtoTableSpec tagSpec = ((DtoTableSpecBuilder) new RegistrationContext().mapToTable("tags")
                .mapField("id").toColumn("ID"))
                .buildDtoTableSpec(TagDto.class);
        TableMapper.MappedTable mappedTag = mapper.mapToTable(MethodHandles.lookup(), TagDto.class, tagSpec.tableSpec());
        when(tableRegistry.getTable(TagDto.class)).thenReturn(mappedTag.ormTable());

        // Register Customer with ManyToMany to Tag
        DtoTableSpec custSpec = ((DtoTableSpecBuilder) new RegistrationContext().mapToTable("customers")
                .mapField("id").toColumn("ID")
                .mapField("tags").manyToMany(b -> b.joinTable("customer_tags").joinColumn("CUST_ID").inverseJoinColumn("TAG_ID")))
                .buildDtoTableSpec(CustomerManyToManyDto.class);

        // When
        TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), CustomerManyToManyDto.class, custSpec.tableSpec());

        // Then
        assertNotNull(result);
    }

    private static class TagDto {
        private Long id;
    }

    private static class CustomerManyToManyDto {
        private Long id;
        private List<TagDto> tags;
    }
}
