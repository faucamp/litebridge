package org.litebridge.spring.repository;

import org.litebridge.orm.LitebridgeCore;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;

public class LitebridgeQueryLookupStrategy implements QueryLookupStrategy {

    private final LitebridgePartTreeQuery.PartTreeSpringSupport partTreeSpringSupport;
    private final LitebridgeCore litebridge;

    public LitebridgeQueryLookupStrategy(final LitebridgePartTreeQuery.PartTreeSpringSupport partTreeSpringSupport,
                                         final LitebridgeCore litebridge) {
        this.partTreeSpringSupport = partTreeSpringSupport;
        this.litebridge = litebridge;
    }

    @Override
    public RepositoryQuery resolveQuery(
            Method method,
            RepositoryMetadata metadata,
            ProjectionFactory projectionFactory,
            NamedQueries namedQueries) {

        // Check if method is derived by name using PartTree
        return new LitebridgePartTreeQuery(method, metadata, projectionFactory, partTreeSpringSupport, litebridge);
    }
}