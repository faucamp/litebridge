package org.litebridgedb.orm.persistence.register;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.generator.ColumnValueGenerator;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.orm.annotation.AllowInterface;
import org.litebridgedb.orm.annotation.Column;
import org.litebridgedb.orm.annotation.ManyToMany;
import org.litebridgedb.orm.annotation.OneToMany;
import org.litebridgedb.orm.annotation.Table;
import org.litebridgedb.orm.api.spec.DtoTableSpec;
import org.mockito.Mockito;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AnnotationMapperTest {

    @Test
    void createDtoTableSpec() {
        // Given
        final DatabaseProvider dbProvider = Mockito.mock(DatabaseProvider.class);

        // When
        final DtoTableSpec result = AnnotationMapper.createDtoTableSpec(ValidEntity.class, dbProvider, MethodHandles.lookup());

        // Then
        assertNotNull(result);
        assertEquals("test_table", result.tableSpec().name());
        assertEquals(2, result.tableSpec().fieldColumnMap().size());
    }

    @Test
    void createDtoTableSpec_noTable() {
        // Given
        final DatabaseProvider dbProvider = Mockito.mock(DatabaseProvider.class);

        // When / Then
        assertThrows(IllegalArgumentException.class, () ->
                AnnotationMapper.createDtoTableSpec(NoTableAnnotation.class, dbProvider, MethodHandles.lookup()));
    }

    @Test
    void createDtoTableSpec_withInterface() {
        // Given
        final DatabaseProvider dbProvider = Mockito.mock(DatabaseProvider.class);

        // When
        final DtoTableSpec result = AnnotationMapper.createDtoTableSpec(EntityWithInterface.class, dbProvider, MethodHandles.lookup());

        // Then
        assertEquals(1, result.dtoInterfaces().size());
        assertEquals(Runnable.class, result.dtoInterfaces().get(0));
    }

    @Test
    void createDtoTableSpec_noAnnotatedFields() {
        // Given
        final DatabaseProvider dbProvider = Mockito.mock(DatabaseProvider.class);

        // When / Then
        assertThrows(IllegalArgumentException.class, () ->
                AnnotationMapper.createDtoTableSpec(NoAnnotatedFields.class, dbProvider, MethodHandles.lookup()));
    }

    @Test
    void createDtoTableSpec_methodAnnotated() {
        // Given
        final DatabaseProvider dbProvider = Mockito.mock(DatabaseProvider.class);

        // When
        final DtoTableSpec result = AnnotationMapper.createDtoTableSpec(MethodAnnotatedEntity.class, dbProvider, MethodHandles.lookup());

        // Then
        assertEquals(3, result.tableSpec().fieldColumnMap().size());
    }

    @Test
    void createDtoTableSpec_oneToMany() {
        // Given
        final DatabaseProvider dbProvider = Mockito.mock(DatabaseProvider.class);

        // When
        final DtoTableSpec result = AnnotationMapper.createDtoTableSpec(OneToManyEntity.class, dbProvider, MethodHandles.lookup());

        // Then
        assertEquals(1, result.tableSpec().fieldColumnMap().size());
    }

    @Test
    void createDtoTableSpec_manyToMany() {
        // Given
        final DatabaseProvider dbProvider = Mockito.mock(DatabaseProvider.class);

        // When
        final DtoTableSpec spec = AnnotationMapper.createDtoTableSpec(ManyToManyEntity.class, dbProvider, MethodHandles.lookup());

        // Then
        assertEquals(1, spec.tableSpec().fieldColumnMap().size());
    }

    @Test
    void createDtoTableSpec_InvalidMethod() {
        // Given
        final DatabaseProvider dbProvider = Mockito.mock(DatabaseProvider.class);

        // When / Then
        assertThrows(IllegalArgumentException.class, () ->
                AnnotationMapper.createDtoTableSpec(InvalidMethodEntity.class, dbProvider, MethodHandles.lookup()));
    }

    @Test
    void createDtoTableSpec_specialColumns() {
        // Given
        final DatabaseProvider dbProvider = Mockito.mock(DatabaseProvider.class);
        final SequenceColumnValueGenerator seqGen = Mockito.mock(SequenceColumnValueGenerator.class);
        when(dbProvider.getSequenceColumnValueGenerator(anyString())).thenReturn(seqGen);

        // When
        DtoTableSpec result = AnnotationMapper.createDtoTableSpec(SpecialColumnEntity.class, dbProvider, MethodHandles.lookup());

        // Then
        assertEquals(4, result.tableSpec().fieldColumnMap().size());
    }

    @Test
    void createDtoTableSpec_failingGenerator() {
        // Given
        final DatabaseProvider dbProvider = Mockito.mock(DatabaseProvider.class);

        // When / Then
        assertThrows(IllegalArgumentException.class, () ->
                AnnotationMapper.createDtoTableSpec(FailingGeneratorEntity.class, dbProvider, MethodHandles.lookup()));
    }

    @Table("test_table")
    static class DoubleAnnotatedEntity {
        @Column("id")
        private Long id;

        @Column("id")
        public Long getId() {
            return id;
        }
    }

    @Table("test_table")
    static class RecordStyleEntity {
        private String name;

        @Column("name")
        public String name() {
            return name;
        }
    }

    @Table("test_table")
    static class ShortGetMethodEntity {
        @Column("id")
        public Long get() {
            return 1L;
        }
    }

    @Test
    void createDtoTableSpec_DoubleAnnotated() {
        DatabaseProvider dbProvider = Mockito.mock(DatabaseProvider.class);
        DtoTableSpec spec = AnnotationMapper.createDtoTableSpec(DoubleAnnotatedEntity.class, dbProvider, MethodHandles.lookup());
        assertEquals(1, spec.tableSpec().fieldColumnMap().size());
    }

    @Test
    void createDtoTableSpec_RecordStyle() {
        DatabaseProvider dbProvider = Mockito.mock(DatabaseProvider.class);
        DtoTableSpec spec = AnnotationMapper.createDtoTableSpec(RecordStyleEntity.class, dbProvider, MethodHandles.lookup());
        assertEquals(1, spec.tableSpec().fieldColumnMap().size());
        assertTrue(spec.tableSpec().fieldColumnMap().keySet().stream().anyMatch(f -> ((org.litebridgedb.orm.api.spec.FieldSpec) f).name().equals("name")));
    }

    @Test
    void createDtoTableSpec_ShortGet() {
        DatabaseProvider dbProvider = Mockito.mock(DatabaseProvider.class);
        DtoTableSpec spec = AnnotationMapper.createDtoTableSpec(ShortGetMethodEntity.class, dbProvider, MethodHandles.lookup());
        assertEquals(1, spec.tableSpec().fieldColumnMap().size());
        assertTrue(spec.tableSpec().fieldColumnMap().keySet().stream().anyMatch(f -> ((org.litebridgedb.orm.api.spec.FieldSpec) f).name().equals("get")));
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<AnnotationMapper> constructor = AnnotationMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        AnnotationMapper instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Table("test_table")
    static class ValidEntity {
        @Column("id")
        private Long id;

        @Column("name")
        private String name;

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @Table("test_table")
    @AllowInterface(Runnable.class)
    static class EntityWithInterface {
        @Column("id")
        private Long id;
    }

    static class NoTableAnnotation {
        @Column("id")
        private Long id;
    }

    @Table("test_table")
    static class NoAnnotatedFields {
        private Long id;
    }

    @Table("test_table")
    static class MethodAnnotatedEntity {
        private Long id;

        @Column("id")
        public Long getId() {
            return id;
        }

        @OneToMany(mappedByField = "parent")
        public java.util.List<String> getChildren() {
            return null;
        }

        @ManyToMany(joinTable = "join_table", joinColumn = "col1", inverseJoinColumn = "col2")
        public java.util.List<String> getOthers() {
            return null;
        }
    }

    @Table("test_table")
    static class OneToManyEntity {
        @OneToMany(mappedByField = "parent")
        private java.util.List<String> children;
    }

    @Table("test_table")
    static class ManyToManyEntity {
        @ManyToMany(joinTable = "join_table", joinColumn = "col1", inverseJoinColumn = "col2")
        private java.util.List<String> others;
    }

    @Table("test_table")
    static class InvalidMethodEntity {
        @Column("id")
        public Long getId(String param) {
            return 1L;
        }
    }

    @Table("test_table")
    static class SpecialColumnEntity {
        @Column(value = "col1", joinUsing = true)
        private String col1;

        @Column(value = "col2", joinOn = "other.id")
        private String col2;

        @Column(value = "col3", generateUsingSequence = "seq_test")
        private Long col3;

        @Column(value = "col4", generator = CustomGenerator.class)
        private String col4;
    }

    public static class CustomGenerator implements ColumnValueGenerator {
        @Override
        public Object generate(org.litebridgedb.db.spi.ColumnMetaData columnMetaData) {
            return "generated";
        }
    }

    @Table("test_table")
    static class FailingGeneratorEntity {
        @Column(value = "col1", generator = FailingGenerator.class)
        private String col1;
    }

    public static class FailingGenerator implements ColumnValueGenerator {
        public FailingGenerator() {
            throw new RuntimeException("Fail");
        }

        @Override
        public Object generate(org.litebridgedb.db.spi.ColumnMetaData columnMetaData) {
            return null;
        }
    }
}
