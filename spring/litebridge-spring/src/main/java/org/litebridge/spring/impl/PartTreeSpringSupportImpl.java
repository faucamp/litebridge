package org.litebridge.spring.impl;

import org.litebridge.spring.repository.LitebridgePartTreeQuery;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.parser.Part;

import java.lang.reflect.Method;

/**
 * Spring 7 support for {@link LitebridgePartTreeQuery}.
 */
final class PartTreeSpringSupportImpl implements LitebridgePartTreeQuery.PartTreeSpringSupport {

    @Override
    public QueryMethod createQueryMethod(final Method method, final RepositoryMetadata metadata, final ProjectionFactory projectionFactory) {
        return new QueryMethod(method, metadata, projectionFactory, null);
    }

    @Override
    public String getPropertyPath(final Part part) {
        return part.getProperty().toDotPath();
    }
}
