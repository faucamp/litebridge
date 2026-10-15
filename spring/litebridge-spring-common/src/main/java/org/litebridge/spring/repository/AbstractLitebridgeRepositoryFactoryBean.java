package org.litebridge.spring.repository;

import org.litebridge.orm.Litebridge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;

import java.io.Serializable;

/**
 * Common implementation of factory beans for {@link LitebridgeRepository} instances.
 *
 * @param <T>  Repository interface type.
 * @param <S>  Entity type.
 * @param <ID> Entity identifier type.
 */
public abstract class AbstractLitebridgeRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends TransactionalRepositoryFactoryBeanSupport<T, S, ID> {

    protected Litebridge litebridge;

    /**
     * Creates a new {@code AbstractLitebridgeRepositoryFactoryBean} instance.
     *
     * @param repositoryInterface The repository interface type.
     */
    public AbstractLitebridgeRepositoryFactoryBean(final Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Autowired
    public void setLitebridge(final Litebridge litebridge) {
        this.litebridge = litebridge;
    }
}
