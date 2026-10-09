package org.litebridge.spring.repository;

import org.litebridge.orm.LitebridgeCore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;

import java.io.Serializable;

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
        protected Object getTargetRepository(final RepositoryInformation metadata) {
            return new LitebridgeRepositoryImpl<>(metadata.getDomainType(), litebridge);
        }

        @Override
        protected Class<?> getRepositoryBaseClass(final RepositoryMetadata metadata) {
            return LitebridgeRepositoryImpl.class;
        }
    }
}
