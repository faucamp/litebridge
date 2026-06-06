package org.litebridgedb.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.orm.api.register.DtoTableSpecBuilder;
import org.litebridgedb.orm.api.register.RegistrationContext;
import org.litebridgedb.orm.api.register.RegistrationContextTerminal;
import org.litebridgedb.orm.api.spec.DtoTableSpec;
import org.litebridgedb.orm.api.spec.TableSpec;
import org.litebridgedb.tracking.ChangeTracker;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TableMapperComplexTest {

    @Test
    void mapToTable_withOneToMany() throws SQLException {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        // Register Order first
        final Table orderTable = new Table("", "public", "orders");
        final ColumnMetaData orderIdColumn = new ColumnMetaData(orderTable, "ID", false, Types.BIGINT);
        final TableMetaData orderMeta = new TableMetaData(orderTable, List.of("ID"), List.of(orderIdColumn));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(orderMeta);

        // When
        final RegistrationContextTerminal context = new RegistrationContext(OrderDto.class, mock(DatabaseProvider.class))
                .mapToTable("orders")
                .with(spec -> spec.mapField("id").toColumn("ID"));
        final DtoTableSpec orderSpec = new DtoTableSpecBuilder(context).build();

        final TableMapper.MappedTable mappedOrder = mapper.mapToTable(MethodHandles.lookup(), OrderDto.class, orderSpec.tableSpec(), Set.of(OrderDto.class));
        when(tableRegistry.getTable(OrderDto.class)).thenReturn(mappedOrder.ormTable());

        // Now register Customer with OneToMany to Order
        final Table customerTable = new Table("", "public", "customers");
        final ColumnMetaData custIdColumn = new ColumnMetaData(customerTable, "ID", false, Types.BIGINT);
        final TableMetaData custMeta = new TableMetaData(customerTable, List.of("ID"), List.of(custIdColumn));
        when(databaseProvider.tableMetaData(any(), any())).thenReturn(custMeta);

        final DtoTableSpec custSpec = new DtoTableSpecBuilder(new RegistrationContext(CustomerDto.class, mock(DatabaseProvider.class)).mapToTable("customers")
                .with(spec -> spec.mapField("id").toColumn("ID"))
                .with(spec -> spec.mapField("orders").oneToMany(b -> b.mappedByField("customer"))))
                .build();

        // When
        final TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), CustomerDto.class, custSpec.tableSpec(), Set.of(CustomerDto.class));

        // Then
        assertNotNull(result);
    }

    @Test
    void mapToTable_invalidDto() {
        // Given
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> mapper.mapToTable(MethodHandles.lookup(), String.class, mock(TableSpec.class), Collections.emptySet()));
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
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
        final TableMapper mapper = new TableMapper(databaseProvider, tableRegistry, changeTracker);

        // Customer
        final Table customerTable = new Table("", "public", "customers");
        final ColumnMetaData custIdColumn = new ColumnMetaData(customerTable, "ID", false, Types.BIGINT);
        final TableMetaData custMeta = new TableMetaData(customerTable, List.of("ID"), List.of(custIdColumn));

        // Tag
        final Table tagTable = new Table("", "public", "tags");
        final ColumnMetaData tagIdColumn = new ColumnMetaData(tagTable, "ID", false, Types.BIGINT);
        final TableMetaData tagMeta = new TableMetaData(tagTable, List.of("ID"), List.of(tagIdColumn));

        // Join Table
        final Table joinTable = new Table("", "public", "customer_tags");
        final ColumnMetaData custJoinCol = new ColumnMetaData(joinTable, "CUST_ID", false, Types.BIGINT);
        final ColumnMetaData tagJoinCol = new ColumnMetaData(joinTable, "TAG_ID", false, Types.BIGINT);
        final TableMetaData joinMeta = new TableMetaData(joinTable, List.of("CUST_ID", "TAG_ID"), List.of(custJoinCol, tagJoinCol));

        when(databaseProvider.tableMetaData(any(), any())).thenAnswer(invocation -> {
            TableSpec spec = invocation.getArgument(0);
            if (spec.name().equals("customers")) return custMeta;
            if (spec.name().equals("tags")) return tagMeta;
            if (spec.name().equals("customer_tags")) return joinMeta;
            return null;
        });

        // Register Tag
        final DtoTableSpec tagSpec = new DtoTableSpecBuilder(new RegistrationContext(TagDto.class, mock(DatabaseProvider.class)).mapToTable("tags")
                .with(spec -> spec.mapField("id").toColumn("ID")))
                .build();
        final TableMapper.MappedTable mappedTag = mapper.mapToTable(MethodHandles.lookup(), TagDto.class, tagSpec.tableSpec(), Set.of(TagDto.class));
        when(tableRegistry.getTable(TagDto.class)).thenReturn(mappedTag.ormTable());

        // Register Customer with ManyToMany to Tag
        final DtoTableSpec custSpec = new DtoTableSpecBuilder(new RegistrationContext(CustomerDto.class, mock(DatabaseProvider.class)).mapToTable("customers")
                .with(spec -> spec.mapField("id").toColumn("ID"))
                .with(spec -> spec.mapField("tags")
                        .manyToMany(b -> b.joinTable("customer_tags")
                                .joinColumn("CUST_ID")
                                .inverseJoinColumn("TAG_ID"))))
                .build();

        // When
        final TableMapper.MappedTable result = mapper.mapToTable(MethodHandles.lookup(), CustomerManyToManyDto.class, custSpec.tableSpec(), Set.of(CustomerManyToManyDto.class));

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
