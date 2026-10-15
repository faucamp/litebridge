package org.litebridge.spring.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.Litebridge;
import org.litebridge.spring.repository.AbstractLitebridgeRepositoryFactory;
import org.litebridge.spring.repository.LitebridgeQueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

import java.util.Optional;

/**
 * Factory for {@link org.litebridge.spring.repository.LitebridgeRepository} instances, Spring Framework 6 implementation.
 */
public final class LitebridgeRepositoryFactory extends AbstractLitebridgeRepositoryFactory {

    /**
     * Creates a new {@code LitebridgeRepositoryFactory} instance.
     *
     * @param litebridge The Litebridge instance to use.
     */
    public LitebridgeRepositoryFactory(final Litebridge litebridge) {
        super(litebridge);
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(
            final QueryLookupStrategy.@Nullable Key key,
            final QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional.of(new LitebridgeQueryLookupStrategy(new PartTreeSpringSupportImpl(), litebridge));
    }
}
