package org.litebridge.spring.impl;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.litebridge.orm.LitebridgeCore;
import org.litebridge.spring.repository.AbstractLitebridgeRepositoryFactory;
import org.litebridge.spring.repository.LitebridgePartTreeQuery;
import org.litebridge.spring.repository.LitebridgeQueryLookupStrategy;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.ValueExpressionDelegate;
import org.springframework.data.repository.query.parser.Part;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Test factory for {@link org.litebridge.spring.repository.LitebridgeRepository} instances, Spring Framework 7 implementation.
 */
@NullMarked
public final class LitebridgeRepositoryFactory extends AbstractLitebridgeRepositoryFactory {

    public LitebridgeRepositoryFactory(final LitebridgeCore litebridge) {
        super(litebridge);
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(
            final QueryLookupStrategy.@Nullable Key key,
            final ValueExpressionDelegate valueExpressionDelegate) {
        return Optional.of(new LitebridgeQueryLookupStrategy(new PartTreeSupportImpl(), litebridge));
    }

    public static class PartTreeSupportImpl implements LitebridgePartTreeQuery.PartTreeSpringSupport {

        @Override
        public QueryMethod createQueryMethod(final Method method, final RepositoryMetadata metadata, final ProjectionFactory projectionFactory) {
            return new QueryMethod(method, metadata, projectionFactory, null);
        }

        @Override
        public String getPropertyPath(final Part part) {
            return part.getProperty().toDotPath();
        }
    }
}
