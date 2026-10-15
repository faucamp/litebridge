package org.litebridge.spring.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.h2.H2DatabaseProvider;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;
import org.litebridge.spring.annotation.Query;
import org.litebridge.spring.impl.LitebridgeRepositoryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.query.Param;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LitebridgeStringQueryTest {

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

        metadata = new DefaultRepositoryMetadata(PersonStringQueryRepository.class);
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

    private LitebridgeStringQuery createQuery(final String methodName, final Class<?>... paramTypes) throws NoSuchMethodException {
        final Method method = PersonStringQueryRepository.class.getMethod(methodName, paramTypes);
        final Query queryAnnotation = method.getAnnotation(Query.class);
        assertNotNull(queryAnnotation, "Method must be annotated with @Query");
        return new LitebridgeStringQuery(method, metadata, projectionFactory, queryAnnotation.value(), queryAnnotation.modifying(), partTreeSpringSupport, litebridge);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_positionalParameters_returnsEntityList() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("findByNamePositional", String.class);

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
    void execute_positionalParameters_returnsSingleEntity() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("findByIdPositional", Long.class);

        // When
        final Person result = (Person) query.execute(new Object[]{1L});

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Alice", result.getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_namedParameters_returnsEntityList() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("findByAgeBetweenExclusiveNamed", int.class, int.class);

        // When
        final List<Person> result = (List<Person>) query.execute(new Object[]{25, 35});

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Alice", result.getFirst().getName());
        assertEquals(30, result.getFirst().getAge());
    }

    @Test
    void execute_namedParameters_returnsSingleEntity() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("findByIdNamed", Long.class);

        // When
        final Person result = (Person) query.execute(new Object[]{2L});

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Bob", result.getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_returnsRowList() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("findRowsByAgePositional", int.class);

        // When
        final List<Row> result = (List<Row>) query.execute(new Object[]{30});

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void execute_returnsSingleRow() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("findRowByIdPositional", Long.class);

        // When
        final Row result = (Row) query.execute(new Object[]{1L});

        // Then
        assertNotNull(result);
        assertEquals("Alice", result.value("NAME"));
    }

    @Test
    void execute_returnsScalarValue() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("getAverageAge");

        // When
        final Object result = query.execute(new Object[]{});

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Number);
        assertEquals(32.5, ((Number) result).doubleValue(), 0.001);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_returnsScalarList() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("getAllNames");

        // When
        final List<String> result = (List<String>) query.execute(new Object[]{});

        // Then
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("Bob"));
    }

    @Test
    void execute_modifyingPositional_updatesDatabase() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("updateAgePositional", Long.class, int.class);

        // When
        final Object result = query.execute(new Object[]{1L, 99});

        // Then
        assertNotNull(result);
        assertTrue(result instanceof UpdateResult);
        assertEquals(1, ((UpdateResult) result).rowsAffected());

        final LitebridgeStringQuery verifyQuery = createQuery("findByIdPositional", Long.class);
        final Person updated = (Person) verifyQuery.execute(new Object[]{1L});
        assertNotNull(updated);
        assertEquals(99, updated.getAge());
    }

    @Test
    void execute_modifyingNamed_updatesDatabase() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("updateAgeNamed", Long.class, int.class);

        // When
        final Object result = query.execute(new Object[]{2L, 88});

        // Then
        assertNotNull(result);
        assertTrue(result instanceof UpdateResult);
        assertEquals(1, ((UpdateResult) result).rowsAffected());

        final LitebridgeStringQuery verifyQuery = createQuery("findByIdNamed", Long.class);
        final Person updated = (Person) verifyQuery.execute(new Object[]{2L});
        assertNotNull(updated);
        assertEquals(88, updated.getAge());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_emptyResult_returnsEmptyListForCollections() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("findByNamePositional", String.class);

        // When
        final List<Person> result = (List<Person>) query.execute(new Object[]{"NonExistent"});

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void execute_emptyResult_returnsDefaultValueForSingleEntity() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("findByIdPositional", Long.class);

        // When
        final Person result = (Person) query.execute(new Object[]{999L});

        // Then
        assertNull(result);
    }

    @Test
    void execute_emptyResult_returnsDefaultValueForScalarPrimitive() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("getCountForNonExistent", String.class);

        // When
        final Object result = query.execute(new Object[]{"NonExistent"});

        // Then
        assertNotNull(result);
        assertEquals(0L, ((Number) result).longValue());
    }

    @Test
    void execute_scalarQueryWithMultipleColumns_throwsIllegalStateException() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("getInvalidMultiColumnScalar");

        // When / Then
        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> query.execute(new Object[]{}));
        assertTrue(exception.getMessage().contains("Expected a single column"));
    }

    @Test
    void execute_scalarNullValue_returnsDefaultValue() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("getEmailById", Long.class);

        // When (Charlie has NULL email)
        final Object result = query.execute(new Object[]{3L});

        // Then
        assertNull(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_namedParametersWithSpecialParameters_skipsSpecialParameters() throws Exception {
        // Given
        final LitebridgeStringQuery query = createQuery("findWithPageable", String.class, Pageable.class);

        // When
        final List<Person> result = (List<Person>) query.execute(new Object[]{"Alice", Pageable.unpaged()});

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
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

    interface PersonStringQueryRepository extends Repository<Person, Long> {

        @Query("SELECT * FROM PERSON WHERE NAME = ?1")
        List<Person> findByNamePositional(String name);

        @Query("SELECT * FROM PERSON WHERE ID = ?1")
        Person findByIdPositional(Long id);

        @Query("SELECT * FROM PERSON WHERE AGE > :minAge AND AGE < :maxAge")
        List<Person> findByAgeBetweenExclusiveNamed(@Param("minAge") int minAge, @Param("maxAge") int maxAge);

        @Query("SELECT * FROM PERSON WHERE ID = :id")
        Person findByIdNamed(@Param("id") Long id);

        @Query("SELECT * FROM PERSON WHERE AGE > ?1")
        List<Row> findRowsByAgePositional(int minAge);

        @Query("SELECT * FROM PERSON WHERE ID = ?1")
        Row findRowByIdPositional(Long id);

        @Query("SELECT AVG(AGE) FROM PERSON")
        double getAverageAge();

        @Query("SELECT NAME FROM PERSON")
        List<String> getAllNames();

        @Query(value = "UPDATE PERSON SET AGE = ?2 WHERE ID = ?1", modifying = true)
        UpdateResult updateAgePositional(Long id, int age);

        @Query(value = "UPDATE PERSON SET AGE = :age WHERE ID = :id", modifying = true)
        UpdateResult updateAgeNamed(@Param("id") Long id, @Param("age") int age);

        @Query("SELECT COUNT(*) FROM PERSON WHERE NAME = ?1 HAVING COUNT(*) > 100")
        long getCountForNonExistent(String name);

        @Query("SELECT NAME, AGE FROM PERSON")
        String getInvalidMultiColumnScalar();

        @Query("SELECT EMAIL FROM PERSON WHERE ID = ?1")
        String getEmailById(Long id);

        @Query("SELECT * FROM PERSON WHERE NAME = :name")
        List<Person> findWithPageable(@Param("name") String name, Pageable pageable);
    }
}
