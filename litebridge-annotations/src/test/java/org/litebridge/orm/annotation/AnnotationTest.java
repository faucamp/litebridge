package org.litebridge.orm.annotation;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.generator.ColumnValueGenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationTest {

    @Test
    void allowInterface_hasExpectedMetaAnnotationsAndRuntimeValues() {
        final Target target = AllowInterface.class.getAnnotation(Target.class);
        final Retention retention = AllowInterface.class.getAnnotation(Retention.class);

        assertArrayEquals(new ElementType[]{ElementType.TYPE}, target.value());
        assertEquals(RetentionPolicy.RUNTIME, retention.value());

        final AllowInterface annotation = EntityWithAllowedInterfaces.class.getAnnotation(AllowInterface.class);

        assertArrayEquals(new Class<?>[]{AllowedContract.class, AnotherAllowedContract.class}, annotation.value());
    }

    @Test
    void table_hasExpectedMetaAnnotationsAndRuntimeValue() {
        final Target target = Table.class.getAnnotation(Target.class);
        final Retention retention = Table.class.getAnnotation(Retention.class);

        assertArrayEquals(new ElementType[]{ElementType.TYPE}, target.value());
        assertEquals(RetentionPolicy.RUNTIME, retention.value());

        final Table annotation = EntityWithTable.class.getAnnotation(Table.class);

        assertEquals("test_table", annotation.value());
    }

    @Test
    void column_hasExpectedMetaAnnotationsAndDefaultValues() throws NoSuchFieldException {
        final Target target = Column.class.getAnnotation(Target.class);
        final Retention retention = Column.class.getAnnotation(Retention.class);

        assertArrayEquals(new ElementType[]{ElementType.FIELD, ElementType.METHOD}, target.value());
        assertEquals(RetentionPolicy.RUNTIME, retention.value());

        final Field field = EntityWithColumnDefaults.class.getDeclaredField("id");
        final Column annotation = field.getAnnotation(Column.class);

        assertEquals("id", annotation.value());
        assertEquals("", annotation.joinOn());
        assertFalse(annotation.joinUsing());
        assertSame(ColumnValueGenerator.class, annotation.generator());
        assertEquals("", annotation.generateUsingSequence());
    }

    @Test
    void column_readsConfiguredFieldValuesAtRuntime() throws NoSuchFieldException {
        final Field field = EntityWithConfiguredColumn.class.getDeclaredField("id");
        final Column annotation = field.getAnnotation(Column.class);

        assertEquals("entity_id", annotation.value());
        assertEquals("owner_id", annotation.joinOn());
        assertTrue(annotation.joinUsing());
        assertSame(TestColumnValueGenerator.class, annotation.generator());
        assertEquals("entity_seq", annotation.generateUsingSequence());
    }

    @Test
    void column_canBeAppliedToMethod() throws NoSuchMethodException {
        final Method method = EntityWithColumnMethod.class.getDeclaredMethod("getName");
        final Column annotation = method.getAnnotation(Column.class);

        assertEquals("name", annotation.value());
        assertEquals("name_join", annotation.joinOn());
        assertFalse(annotation.joinUsing());
        assertSame(TestColumnValueGenerator.class, annotation.generator());
        assertEquals("name_seq", annotation.generateUsingSequence());
    }

    @Test
    void oneToMany_hasExpectedMetaAnnotationsAndRuntimeValuesOnFieldAndMethod() throws NoSuchFieldException, NoSuchMethodException {
        final Target target = OneToMany.class.getAnnotation(Target.class);
        final Retention retention = OneToMany.class.getAnnotation(Retention.class);

        assertArrayEquals(new ElementType[]{ElementType.FIELD, ElementType.METHOD}, target.value());
        assertEquals(RetentionPolicy.RUNTIME, retention.value());

        final Field field = EntityWithRelationships.class.getDeclaredField("children");
        final OneToMany fieldAnnotation = field.getAnnotation(OneToMany.class);

        assertEquals("parent", fieldAnnotation.mappedByField());

        final Method method = EntityWithRelationships.class.getDeclaredMethod("getOtherChildren");
        final OneToMany methodAnnotation = method.getAnnotation(OneToMany.class);

        assertEquals("otherParent", methodAnnotation.mappedByField());
    }

    @Test
    void manyToMany_hasExpectedMetaAnnotationsAndRuntimeValuesOnFieldAndMethod() throws NoSuchFieldException, NoSuchMethodException {
        final Target target = ManyToMany.class.getAnnotation(Target.class);
        final Retention retention = ManyToMany.class.getAnnotation(Retention.class);

        assertArrayEquals(new ElementType[]{ElementType.FIELD, ElementType.METHOD}, target.value());
        assertEquals(RetentionPolicy.RUNTIME, retention.value());

        final Field field = EntityWithRelationships.class.getDeclaredField("groups");
        final ManyToMany fieldAnnotation = field.getAnnotation(ManyToMany.class);

        assertEquals("entity_group", fieldAnnotation.joinTable());
        assertEquals("entity_id", fieldAnnotation.joinColumn());
        assertEquals("group_id", fieldAnnotation.inverseJoinColumn());

        final Method method = EntityWithRelationships.class.getDeclaredMethod("getRoles");
        final ManyToMany methodAnnotation = method.getAnnotation(ManyToMany.class);

        assertEquals("entity_role", methodAnnotation.joinTable());
        assertEquals("entity_id", methodAnnotation.joinColumn());
        assertEquals("role_id", methodAnnotation.inverseJoinColumn());
    }

    interface AllowedContract {
    }

    interface AnotherAllowedContract {
    }

    @AllowInterface({AllowedContract.class, AnotherAllowedContract.class})
    private static final class EntityWithAllowedInterfaces {
    }

    @Table("test_table")
    private static final class EntityWithTable {
    }

    private static final class EntityWithColumnDefaults {

        @Column("id")
        private Long id;
    }

    private static final class EntityWithConfiguredColumn {

        @Column(
                value = "entity_id",
                joinOn = "owner_id",
                joinUsing = true,
                generator = TestColumnValueGenerator.class,
                generateUsingSequence = "entity_seq"
        )
        private Long id;
    }

    private static final class EntityWithColumnMethod {

        @Column(
                value = "name",
                joinOn = "name_join",
                generator = TestColumnValueGenerator.class,
                generateUsingSequence = "name_seq"
        )
        String getName() {
            return "name";
        }
    }

    private static final class EntityWithRelationships {

        @OneToMany(mappedByField = "parent")
        private List<ChildEntity> children;

        @ManyToMany(
                joinTable = "entity_group",
                joinColumn = "entity_id",
                inverseJoinColumn = "group_id"
        )
        private List<GroupEntity> groups;

        @OneToMany(mappedByField = "otherParent")
        List<ChildEntity> getOtherChildren() {
            return List.of();
        }

        @ManyToMany(
                joinTable = "entity_role",
                joinColumn = "entity_id",
                inverseJoinColumn = "role_id"
        )
        List<RoleEntity> getRoles() {
            return List.of();
        }
    }

    private static final class ChildEntity {
    }

    private static final class GroupEntity {
    }

    private static final class RoleEntity {
    }

    public static final class TestColumnValueGenerator implements ColumnValueGenerator {

        @Override
        public Object generate(final ColumnMetaData columnMetaData) {
            return "generated";
        }
    }
}