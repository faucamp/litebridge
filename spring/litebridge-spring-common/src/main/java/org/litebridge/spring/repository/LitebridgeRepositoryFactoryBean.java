package org.litebridge.spring.repository;

import org.litebridge.orm.LitebridgeCore;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.ClassUtils;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * Factory bean for {@link LitebridgeRepository} instances.
 *
 * @param <T>  Repository interface type.
 * @param <S>  Entity type.
 * @param <ID> Entity identifier type.
 */
public class LitebridgeRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends AbstractLitebridgeRepositoryFactoryBean<T, S, ID> {

    /**
     * Creates a new {@code LitebridgeRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface Repository interface to build.
     */
    public LitebridgeRepositoryFactoryBean(final Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport doCreateRepositoryFactory() {
        return createRepositoryFactoryImpl();
    }

    @SuppressWarnings("unchecked")
    private <LRF extends RepositoryFactorySupport> RepositoryFactorySupport createRepositoryFactoryImpl() {
        final Class<LRF> repositoryFactoryClass;

        try {
            repositoryFactoryClass = (Class<LRF>) ClassUtils.forName("org.litebridge.spring.impl.LitebridgeRepositoryFactory", getClass().getClassLoader());
        } catch (Exception o_O) {
            throw new IllegalStateException("Class 'org.litebridge.spring.impl.LitebridgeRepositoryFactoryBean' not found");
        }

        final Constructor<LRF> constructor = Objects.requireNonNull(
                ClassUtils.getConstructorIfAvailable(repositoryFactoryClass, LitebridgeCore.class),
                "Constructor not found for class 'org.litebridge.spring.impl.LitebridgeRepositoryFactoryBean'");

        return org.litebridge.commons.ClassUtils.newInstance(repositoryFactoryClass, constructor, litebridge);
    }
}
