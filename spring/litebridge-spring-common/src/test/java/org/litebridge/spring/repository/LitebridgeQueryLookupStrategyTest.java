package org.litebridge.spring.repository;

import org.junit.jupiter.api.Test;
import org.litebridge.db.h2.H2DatabaseProvider;
import org.litebridge.orm.LitebridgeCore;
import org.litebridge.spring.annotation.Query;
import org.litebridge.spring.impl.LitebridgeRepositoryFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.query.RepositoryQuery;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class LitebridgeQueryLookupStrategyTest {

    interface TestRepository extends Repository<TestEntity, Long> {

        @Query("SELECT * FROM TEST_ENTITY WHERE NAME = :name")
        List<TestEntity> findCustomQuery(String name);

        List<TestEntity> findByName(String name);
    }

    static class TestEntity {
        private Long id;
        private String name;
    }

    @Test
    void resolveQuery_withQueryAnnotation_returnsLitebridgeStringQuery() throws NoSuchMethodException {
        // Given
        final LitebridgeCore litebridge = new LitebridgeCore(new H2DatabaseProvider(), mock(DataSource.class));
        final LitebridgePartTreeQuery.PartTreeSpringSupport partTreeSupport = new LitebridgeRepositoryFactory.PartTreeSupportImpl();
        final LitebridgeQueryLookupStrategy strategy = new LitebridgeQueryLookupStrategy(partTreeSupport, litebridge);
        final DefaultRepositoryMetadata metadata = new DefaultRepositoryMetadata(TestRepository.class);
        final SpelAwareProxyProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
        final NamedQueries namedQueries = mock(NamedQueries.class);
        final Method method = TestRepository.class.getMethod("findCustomQuery", String.class);

        // When
        final RepositoryQuery query = strategy.resolveQuery(method, metadata, projectionFactory, namedQueries);

        // Then
        assertNotNull(query);
        assertInstanceOf(LitebridgeStringQuery.class, query);
    }

    @Test
    void resolveQuery_withoutQueryAnnotation_returnsLitebridgePartTreeQuery() throws NoSuchMethodException {
        // Given
        final LitebridgeCore litebridge = new LitebridgeCore(new H2DatabaseProvider(), mock(DataSource.class));
        final LitebridgePartTreeQuery.PartTreeSpringSupport partTreeSupport = new LitebridgeRepositoryFactory.PartTreeSupportImpl();
        final LitebridgeQueryLookupStrategy strategy = new LitebridgeQueryLookupStrategy(partTreeSupport, litebridge);
        final DefaultRepositoryMetadata metadata = new DefaultRepositoryMetadata(TestRepository.class);
        final SpelAwareProxyProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
        final NamedQueries namedQueries = mock(NamedQueries.class);
        final Method method = TestRepository.class.getMethod("findByName", String.class);

        // When
        final RepositoryQuery query = strategy.resolveQuery(method, metadata, projectionFactory, namedQueries);

        // Then
        assertNotNull(query);
        assertInstanceOf(LitebridgePartTreeQuery.class, query);
    }
}
