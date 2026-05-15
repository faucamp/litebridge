package org.litebridgedb.orm.api.register;

import org.junit.jupiter.api.Test;
import org.litebridgedb.orm.api.spec.DtoTableSpec;
import org.litebridgedb.orm.api.spec.FieldSpec;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrationTest {

    @Test
    void fullRegistrationFlow() {
        // Given
        RegistrationContext context = new RegistrationContext();

        // When
        DtoTableSpec spec = ((DtoTableSpecBuilder) context
                .allowInterface(TestInterface.class)
                .mapToTable("test_table")
                .mapField("id").toColumn("ID").autoIncrement().natively()
                .mapField("name").toColumn("NAME")
                .mapField("address").toColumn("ADDR")
                .mapField("category").toColumn("CAT_ID").joinOn("CAT_ID")
                .mapField("items").oneToMany(b -> b.mappedByField("order"))
                .mapField("tags").manyToMany(b -> b.joinTable("ORDER_TAGS").joinColumn("ORDER_ID").inverseJoinColumn("TAG_ID")))
                .buildDtoTableSpec(TestDto.class);

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
        RegistrationContext context = new RegistrationContext();

        // When
        DtoTableSpec spec = ((DtoTableSpecBuilder) context
                .mapToTable("test_table")
                .mapProperty("name").toColumn("NAME"))
                .buildDtoTableSpec(TestDto.class);

        // Then
        assertNotNull(spec);
        assertTrue(spec.tableSpec().fieldColumnMap().keySet().stream()
                .filter(f -> f instanceof FieldSpec)
                .map(f -> (FieldSpec) f)
                .anyMatch(f -> f.name().equals("name") && f.property()));
    }

    private interface TestInterface {}
    private static class TestDto {
        private Long id;
        private String name;
        private String address;
        private Object category;
        private List<Object> items;
        private List<Object> tags;
    }
}
