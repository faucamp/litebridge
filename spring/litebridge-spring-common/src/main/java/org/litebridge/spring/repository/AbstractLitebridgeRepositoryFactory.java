package org.litebridge.spring.repository;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.Litebridge;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

/**
 * Common implementation for factories of {@link org.litebridge.spring.repository.LitebridgeRepository} instances.
 */
public abstract class AbstractLitebridgeRepositoryFactory extends RepositoryFactorySupport {

    protected final Litebridge litebridge;

    /**
     * Creates a new {@code AbstractLitebridgeRepositoryFactory} instance.
     *
     * @param litebridge The Litebridge instance to use.
     */
    public AbstractLitebridgeRepositoryFactory(final Litebridge litebridge) {
        this.litebridge = litebridge;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, ID> EntityInformation<T, ID> getEntityInformation(final Class<T> domainClass) {
        return new AbstractEntityInformation<>(domainClass) {
            @Override
            public @Nullable ID getId(final T entity) {
                return litebridge.inspect().getPrimaryKey(entity);
            }

            @Override
            public Class<ID> getIdType() {
                return (Class<ID>) litebridge.inspect().getPrimaryKeyType(domainClass);
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
}
