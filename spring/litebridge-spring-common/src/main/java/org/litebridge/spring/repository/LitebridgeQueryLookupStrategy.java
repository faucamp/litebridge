package org.litebridge.spring.repository;

import org.litebridge.orm.Litebridge;
import org.litebridge.spring.annotation.Query;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;

public class LitebridgeQueryLookupStrategy implements QueryLookupStrategy {

    private final LitebridgePartTreeQuery.PartTreeSpringSupport partTreeSpringSupport;
    private final Litebridge litebridge;

    public LitebridgeQueryLookupStrategy(final LitebridgePartTreeQuery.PartTreeSpringSupport partTreeSpringSupport,
                                         final Litebridge litebridge) {
        this.partTreeSpringSupport = partTreeSpringSupport;
        this.litebridge = litebridge;
    }

    @Override
    public RepositoryQuery resolveQuery(
            Method method,
            RepositoryMetadata metadata,
            ProjectionFactory projectionFactory,
            NamedQueries namedQueries) {

        // Check for Litebridge @Query annotation
        final Query queryAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, Query.class);

        if (queryAnnotation != null) {
            return new LitebridgeStringQuery(
                    method,
                    metadata,
                    projectionFactory,
                    queryAnnotation.value(),
                    queryAnnotation.modifying(),
                    partTreeSpringSupport,
                    litebridge
            );
        }

        // Fall back on queries derived by method name using PartTree
        return new LitebridgePartTreeQuery(method, metadata, projectionFactory, partTreeSpringSupport, litebridge);
    }
}