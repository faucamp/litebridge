package org.litebridge.spring.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.LitebridgeCore;
import org.litebridge.spring.repository.AbstractLitebridgeRepositoryFactory;
import org.litebridge.spring.repository.LitebridgeQueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.ValueExpressionDelegate;

import java.util.Optional;

/**
 * Factory for {@link org.litebridge.spring.repository.LitebridgeRepository} instances, Spring Framework 7 implementation.
 */
public final class LitebridgeRepositoryFactory extends AbstractLitebridgeRepositoryFactory {

    /**
     * Creates a new {@code LitebridgeRepositoryFactory} instance.
     *
     * @param litebridge The Litebridge instance to use.
     */
    public LitebridgeRepositoryFactory(final LitebridgeCore litebridge) {
        super(litebridge);
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(
            final QueryLookupStrategy.@Nullable Key key,
            final ValueExpressionDelegate valueExpressionDelegate) {
        return Optional.of(new LitebridgeQueryLookupStrategy(new PartTreeSpringSupportImpl(), litebridge));
    }
}
