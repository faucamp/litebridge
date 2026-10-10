package org.litebridge.spring.repository;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.LitebridgeCore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.ValueExpressionDelegate;

import java.io.Serializable;
import java.util.Optional;

public class LitebridgeRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends TransactionalRepositoryFactoryBeanSupport<T, S, ID> {

    private LitebridgeCore litebridge;

    public LitebridgeRepositoryFactoryBean(final Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Autowired
    public void setLitebridge(final LitebridgeCore litebridge) {
        this.litebridge = litebridge;
    }

    @Override
    protected RepositoryFactorySupport doCreateRepositoryFactory() {
        return new LitebridgeRepositoryFactory(litebridge);
    }

    private static class LitebridgeRepositoryFactory extends RepositoryFactorySupport {

        private final LitebridgeCore litebridge;

        public LitebridgeRepositoryFactory(final LitebridgeCore litebridge) {
            this.litebridge = litebridge;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T, ID> EntityInformation<T, ID> getEntityInformation(final Class<T> domainClass) {
            return new AbstractEntityInformation<>(domainClass) {
                @Override
                public @Nullable ID getId(final T entity) {
                    return null;
                }

                @Override
                public Class<ID> getIdType() {
                    return (Class<ID>) Serializable.class;
                }
            };
        }

        @Override
        protected Object getTargetRepository(final RepositoryInformation metadata) {
            return new LitebridgeRepositoryImpl<>(metadata.getDomainType(), litebridge);
        }

        @Override
        protected Class<?> getRepositoryBaseClass(final RepositoryMetadata metadata) {
            return LitebridgeRepositoryImpl.class;
        }

        @Override
        public <T> T getRepository(final Class<T> repositoryInterface, final RepositoryComposition.RepositoryFragments fragments) {
            return super.getRepository(repositoryInterface, fragments);
        }

        @Override
        @SuppressWarnings("deprecation")
        protected Optional<QueryLookupStrategy> getQueryLookupStrategy(
                final QueryLookupStrategy.@Nullable Key key,
                final QueryMethodEvaluationContextProvider evaluationContextProvider) {
            return Optional.of(new LitebridgeQueryLookupStrategy(litebridge));
        }

        @Override
        protected Optional<QueryLookupStrategy> getQueryLookupStrategy(
                final QueryLookupStrategy.@Nullable Key key,
                final ValueExpressionDelegate valueExpressionDelegate) {
            return Optional.of(new LitebridgeQueryLookupStrategy(litebridge));
        }
    }
}
