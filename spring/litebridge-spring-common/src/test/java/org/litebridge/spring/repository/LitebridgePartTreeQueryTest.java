package org.litebridge.spring.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.h2.H2DatabaseProvider;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;
import org.litebridge.spring.impl.LitebridgeRepositoryFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.lang.invoke.MethodHandles;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LitebridgePartTreeQueryTest {

    private DriverManagerDataSource dataSource;
    private Litebridge litebridge;
    private DefaultRepositoryMetadata metadata;
    private SpelAwareProxyProjectionFactory projectionFactory;
    private LitebridgePartTreeQuery.PartTreeSpringSupport partTreeSpringSupport;

    @BeforeEach
    void setUp() throws Exception {
        final String dbName = "test_" + UUID.randomUUID().toString().replace("-", "");
        dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        try (final Connection conn = dataSource.getConnection();
             final Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE PERSON (" +
                    "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "NAME VARCHAR(255), " +
                    "AGE INT, " +
                    "EMAIL VARCHAR(255), " +
                    "ACTIVE BOOLEAN)");

            stmt.execute("INSERT INTO PERSON (ID, NAME, AGE, EMAIL, ACTIVE) VALUES (1, 'Alice', 30, 'alice@test.com', true)");
            stmt.execute("INSERT INTO PERSON (ID, NAME, AGE, EMAIL, ACTIVE) VALUES (2, 'Bob', 25, 'bob@test.com', false)");
            stmt.execute("INSERT INTO PERSON (ID, NAME, AGE, EMAIL, ACTIVE) VALUES (3, 'Charlie', 35, NULL, true)");
            stmt.execute("INSERT INTO PERSON (ID, NAME, AGE, EMAIL, ACTIVE) VALUES (4, 'Alice', 40, 'alice2@test.com', true)");
        }

        litebridge = new Litebridge(new H2DatabaseProvider(), dataSource, null, MethodHandles.privateLookupIn(Person.class, MethodHandles.lookup()));
        litebridge.register(Person.class);

        metadata = new DefaultRepositoryMetadata(PersonRepository.class);
        projectionFactory = new SpelAwareProxyProjectionFactory();
        partTreeSpringSupport = new LitebridgeRepositoryFactory.PartTreeSupportImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        try (final Connection conn = dataSource.getConnection();
             final Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS PERSON");
        }
    }

    private LitebridgePartTreeQuery createQuery(final String methodName, final Class<?>... paramTypes) throws NoSuchMethodException {
        final Method method = PersonRepository.class.getMethod(methodName, paramTypes);
        return new LitebridgePartTreeQuery(method, metadata, projectionFactory, partTreeSpringSupport, litebridge);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_findByName_returnsMatchingRecords() throws Exception {
        // Given
        final LitebridgePartTreeQuery query = createQuery("findByName", String.class);

        // When
        final List<Person> result = (List<Person>) query.execute(new Object[]{"Alice"});

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals("Alice", result.get(1).getName());
        assertNotNull(query.getQueryMethod());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_findByNameNot_excludesMatches() throws Exception {
        // Given
        final LitebridgePartTreeQuery query = createQuery("findByNameNot", String.class);

        // When
        final List<Person> result = (List<Person>) query.execute(new Object[]{"Alice"});

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Bob", result.get(0).getName());
        assertEquals("Charlie", result.get(1).getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_findByAgeComparisons() throws Exception {
        final LitebridgePartTreeQuery gt = createQuery("findByAgeGreaterThan", int.class);
        final List<Person> gtResult = (List<Person>) gt.execute(new Object[]{28});
        assertEquals(3, gtResult.size());

        final LitebridgePartTreeQuery gte = createQuery("findByAgeGreaterThanEqual", int.class);
        final List<Person> gteResult = (List<Person>) gte.execute(new Object[]{30});
        assertEquals(3, gteResult.size());

        final LitebridgePartTreeQuery lt = createQuery("findByAgeLessThan", int.class);
        final List<Person> ltResult = (List<Person>) lt.execute(new Object[]{30});
        assertEquals(1, ltResult.size());
        assertEquals("Bob", ltResult.get(0).getName());

        final LitebridgePartTreeQuery lte = createQuery("findByAgeLessThanEqual", int.class);
        final List<Person> lteResult = (List<Person>) lte.execute(new Object[]{30});
        assertEquals(2, lteResult.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_findByNameLikePatterns() throws Exception {
        final LitebridgePartTreeQuery like = createQuery("findByNameLike", String.class);
        final List<Person> likeResult = (List<Person>) like.execute(new Object[]{"lic"});
        assertEquals(2, likeResult.size());

        final LitebridgePartTreeQuery notLike = createQuery("findByNameNotLike", String.class);
        final List<Person> notLikeResult = (List<Person>) notLike.execute(new Object[]{"lic"});
        assertEquals(2, notLikeResult.size());

        final LitebridgePartTreeQuery starting = createQuery("findByNameStartingWith", String.class);
        final List<Person> startResult = (List<Person>) starting.execute(new Object[]{"Al"});
        assertEquals(2, startResult.size());

        final LitebridgePartTreeQuery ending = createQuery("findByNameEndingWith", String.class);
        final List<Person> endResult = (List<Person>) ending.execute(new Object[]{"ie"});
        assertEquals(1, endResult.size());
        assertEquals("Charlie", endResult.get(0).getName());

        final LitebridgePartTreeQuery containing = createQuery("findByNameContaining", String.class);
        final List<Person> contResult = (List<Person>) containing.execute(new Object[]{"ob"});
        assertEquals(1, contResult.size());
        assertEquals("Bob", contResult.get(0).getName());

        final LitebridgePartTreeQuery notContaining = createQuery("findByNameNotContaining", String.class);
        final List<Person> notContResult = (List<Person>) notContaining.execute(new Object[]{"lic"});
        assertEquals(2, notContResult.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_findByEmailNullability() throws Exception {
        final LitebridgePartTreeQuery isNull = createQuery("findByEmailIsNull");
        final List<Person> nullResult = (List<Person>) isNull.execute(new Object[]{});
        assertEquals(1, nullResult.size());
        assertEquals("Charlie", nullResult.get(0).getName());

        final LitebridgePartTreeQuery isNotNull = createQuery("findByEmailIsNotNull");
        final List<Person> notNullResult = (List<Person>) isNotNull.execute(new Object[]{});
        assertEquals(3, notNullResult.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_findByAgeBetween() throws Exception {
        final LitebridgePartTreeQuery between = createQuery("findByAgeBetween", int.class, int.class);
        final List<Person> result = (List<Person>) between.execute(new Object[]{24, 31});
        assertEquals(2, result.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_findByAgeInAndNotIn() throws Exception {
        final LitebridgePartTreeQuery inQuery = createQuery("findByAgeIn", List.class);
        final List<Person> inResult = (List<Person>) inQuery.execute(new Object[]{List.of(25, 35)});
        assertEquals(2, inResult.size());

        final LitebridgePartTreeQuery notInQuery = createQuery("findByAgeNotIn", List.class);
        final List<Person> notInResult = (List<Person>) notInQuery.execute(new Object[]{List.of(25, 35)});
        assertEquals(2, notInResult.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_findByActiveBoolean() throws Exception {
        final LitebridgePartTreeQuery trueQuery = createQuery("findByActiveTrue");
        final List<Person> trueResult = (List<Person>) trueQuery.execute(new Object[]{});
        assertEquals(3, trueResult.size());

        final LitebridgePartTreeQuery falseQuery = createQuery("findByActiveFalse");
        final List<Person> falseResult = (List<Person>) falseQuery.execute(new Object[]{});
        assertEquals(1, falseResult.size());
        assertEquals("Bob", falseResult.get(0).getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_findCompoundAndOrConditions() throws Exception {
        final LitebridgePartTreeQuery andQuery = createQuery("findByNameAndAge", String.class, int.class);
        final List<Person> andResult = (List<Person>) andQuery.execute(new Object[]{"Alice", 30});
        assertEquals(1, andResult.size());
        assertEquals(1L, andResult.get(0).getId());

        final LitebridgePartTreeQuery orQuery = createQuery("findByNameOrAge", String.class, int.class);
        final List<Person> orResult = (List<Person>) orQuery.execute(new Object[]{"Bob", 30});
        assertEquals(2, orResult.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_findBySorting() throws Exception {
        final LitebridgePartTreeQuery ascQuery = createQuery("findByNameOrderByNameAsc", String.class);
        final List<Person> ascResult = (List<Person>) ascQuery.execute(new Object[]{"Alice"});
        assertEquals(2, ascResult.size());

        final LitebridgePartTreeQuery descQuery = createQuery("findByNameOrderByNameDesc", String.class);
        final List<Person> descResult = (List<Person>) descQuery.execute(new Object[]{"Alice"});
        assertEquals(2, descResult.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_findWithLimit() throws Exception {
        final LitebridgePartTreeQuery limitQuery = createQuery("findFirst10ByName", String.class);
        final List<Person> limitResult = (List<Person>) limitQuery.execute(new Object[]{"Alice"});
        assertEquals(2, limitResult.size());

        final LitebridgePartTreeQuery singleEntityQuery = createQuery("findFirstByName", String.class);
        final Person single = (Person) singleEntityQuery.execute(new Object[]{"Bob"});
        assertNotNull(single);
        assertEquals("Bob", single.getName());
    }

    @Test
    void execute_countAndExists() throws Exception {
        final LitebridgePartTreeQuery countQuery = createQuery("countByName", String.class);
        final long count = (long) countQuery.execute(new Object[]{"Alice"});
        assertEquals(2L, count);

        final LitebridgePartTreeQuery existsQuery = createQuery("existsByName", String.class);
        final boolean exists = (boolean) existsQuery.execute(new Object[]{"Alice"});
        assertTrue(exists);

        final boolean notExists = (boolean) existsQuery.execute(new Object[]{"NonExistent"});
        assertFalse(notExists);
    }

    @Table("PUBLIC.PERSON")
    public static class Person {
        @Column("ID")
        private Long id;
        @Column("NAME")
        private String name;
        @Column("AGE")
        private int age;
        @Column("EMAIL")
        private String email;
        @Column("ACTIVE")
        private boolean active;

        public Person() {
        }

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(final int age) {
            this.age = age;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(final String email) {
            this.email = email;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(final boolean active) {
            this.active = active;
        }
    }

    interface PersonRepository extends Repository<Person, Long> {
        List<Person> findByName(String name);

        List<Person> findByNameNot(String name);

        List<Person> findByAgeGreaterThan(int age);

        List<Person> findByAgeGreaterThanEqual(int age);

        List<Person> findByAgeLessThan(int age);

        List<Person> findByAgeLessThanEqual(int age);

        List<Person> findByNameLike(String name);

        List<Person> findByNameNotLike(String name);

        List<Person> findByNameStartingWith(String prefix);

        List<Person> findByNameEndingWith(String suffix);

        List<Person> findByNameContaining(String infix);

        List<Person> findByNameNotContaining(String infix);

        List<Person> findByEmailIsNull();

        List<Person> findByEmailIsNotNull();

        List<Person> findByAgeBetween(int min, int max);

        List<Person> findByAgeIn(List<Integer> ages);

        List<Person> findByAgeNotIn(List<Integer> ages);

        List<Person> findByActiveTrue();

        List<Person> findByActiveFalse();

        List<Person> findByNameAndAge(String name, int age);

        List<Person> findByNameOrAge(String name, int age);

        List<Person> findByNameOrderByNameAsc(String name);

        List<Person> findByNameOrderByNameDesc(String name);

        List<Person> findFirst10ByName(String name);

        Person findFirstByName(String name);

        long countByName(String name);

        boolean existsByName(String name);
    }
}
