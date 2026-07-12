package org.litebridge.orm.api.register;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.orm.api.spec.DtoTableSpec;
import org.litebridge.orm.api.spec.FieldSpec;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RegistrationTest {

    @Test
    void fullRegistrationFlow() {
        // Given
        final RegistrationContext context = new RegistrationContext(TestDto.class, mock(DatabaseProvider.class));

        // When
        DtoTableSpec spec = new DtoTableSpecBuilder(context
                .allowInterface(TestInterface.class)
                .mapToTable("test_table")
                .with(s -> s.mapField("id").toColumn("ID"))
                .with(s -> s.mapField("name").toColumn("NAME"))
                .with(s -> s.mapField("address").toColumn("ADDR"))
                .with(s -> s.mapField("category").toColumn("CAT_ID").joinOn("CAT_ID"))
                .with(s -> s.mapField("items").oneToMany(b -> b.mappedByField("order")))
                .with(s -> s.mapField("tags").manyToMany(b -> b.joinTable("ORDER_TAGS").joinColumn("ORDER_ID").inverseJoinColumn("TAG_ID"))))
                .build();

        // Then
        assertNotNull(spec);
        assertEquals(TestDto.class, spec.dtoClass());
        assertEquals("test_table", spec.tableSpec().name());
        assertEquals(List.of(TestInterface.class), spec.dtoInterfaces());
        assertEquals(6, spec.tableSpec().fieldColumnMap().size());
    }

    @Test
    void mapProperty() {
        // Given
        final RegistrationContext context = new RegistrationContext(TestDto.class, mock(DatabaseProvider.class));

        // When
        final DtoTableSpec spec = new DtoTableSpecBuilder(context
                .mapToTable("test_table")
                .with(s -> s.mapProperty("name").toColumn("NAME")))
                .build();

        // Then
        assertNotNull(spec);
        assertTrue(spec.tableSpec().fieldColumnMap().keySet().stream()
                .filter(f -> f instanceof FieldSpec)
                .map(f -> (FieldSpec) f)
                .anyMatch(f -> f.name().equals("name") && f.property()));
    }

    private interface TestInterface {
    }

    private static class TestDto {
        private Long id;
        private String name;
        private String address;
        private Object category;
        private List<Object> items;
        private List<Object> tags;
    }
}
