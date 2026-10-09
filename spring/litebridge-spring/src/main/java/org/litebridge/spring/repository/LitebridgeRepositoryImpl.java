package org.litebridge.spring.repository;

import org.litebridge.orm.LitebridgeCore;
import org.litebridge.orm.api.select.OrderByClauseTerminal;
import org.litebridge.orm.api.select.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.select.dto.DtoOrderByClauseChain;
import org.litebridge.orm.expression.Fn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class LitebridgeRepositoryImpl<T, ID> implements LitebridgeRepository<T, ID> {

    private final LitebridgeCore litebridge;
    private Class<T> entityClass;

    public LitebridgeRepositoryImpl(final Class<T> entityClass, final LitebridgeCore litebridge) {
        this.litebridge = litebridge;
        this.entityClass = entityClass;
    }

    @Override
    public <S extends T> S save(final S entity) {
        litebridge.save(entity);
        return entity;
    }

    @Override
    public <S extends T> List<S> saveAll(final Iterable<S> entities) {
        litebridge.saveAll(entities);

        if (entities instanceof List<S> list) {
            return list;
        }

        return StreamSupport.stream(entities.spliterator(), false).toList();
    }

    @Override
    public Optional<T> findById(final ID id) {
        return litebridge.select(entityClass).withId(id);
    }

    @Override
    public boolean existsById(final ID id) {
        return litebridge.select(Fn.count()).from(entityClass).withIdOrThrow(id) > 0;
    }

    @Override
    public List<T> findAll() {
        return litebridge.select(entityClass).list();
    }

    @Override
    public List<T> findAllById(final Iterable<ID> ids) {
        return litebridge.select(entityClass).withIds(ids).list();
    }

    @Override
    public long count() {
        return litebridge.select(Fn.count()).from(entityClass).oneOrThrow();
    }

    @Override
    public void deleteById(final ID id) {
        litebridge.delete(entityClass, q -> q.withId(id));
    }

    @Override
    public void delete(final T entity) {
        litebridge.delete(entity);
    }

    @Override
    public void deleteAllById(final Iterable<? extends ID> ids) {
        litebridge.delete(entityClass, q -> q.withIds(ids));
    }

    @Override
    public void deleteAll(final Iterable<? extends T> entities) {
        for (final T entity : entities) {
            litebridge.delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        litebridge.delete(entityClass);
    }

    @Override
    public List<T> findAll(final Sort sort) {
        if (sort.isUnsorted()) {
            return findAll();
        }

        final DtoFromClauseTerminal<T> query = litebridge.select(entityClass);
        final DtoOrderByClauseChain<T> orderByClauseChain = addOrderByClauseFromSort(sort, query);
        return orderByClauseChain.list();
    }

    @Override
    public Page<T> findAll(final Pageable pageable) {
        if (pageable.isUnpaged()) {
            final List<T> records = findAll(pageable.getSort());
            return new PageImpl<>(records, pageable, records.size());
        }

        final DtoFromClauseTerminal<T> query = litebridge.select(entityClass);
        OrderByClauseTerminal<T> orderByClauseTerminal = query;

        if (pageable.getSort().isSorted()) {
            orderByClauseTerminal = addOrderByClauseFromSort(pageable.getSort(), query);
        }

        final List<T> records = orderByClauseTerminal
                .limit(pageable.getPageSize())
                .offset((int) pageable.getOffset())
                .list();

        return new PageImpl<>(records, pageable, count());
    }

    private static <T> DtoOrderByClauseChain<T> addOrderByClauseFromSort(final Sort sort,
                                                                         final DtoFromClauseTerminal<T> query) {
        DtoOrderByClauseChain<T> orderByClauseChain = null;

        for (final Sort.Order order : sort) {
            final String property = order.getProperty();
            final Sort.Direction direction = order.getDirection();

            if (orderByClauseChain == null) {
                if (direction.isAscending()) {
                    orderByClauseChain = query.orderBy(property).asc();
                } else {
                    orderByClauseChain = query.orderBy(property).desc();
                }
            } else {
                if (direction.isAscending()) {
                    orderByClauseChain = orderByClauseChain.then(property).asc();
                } else {
                    orderByClauseChain = orderByClauseChain.then(property).desc();
                }
            }
        }

        return Objects.requireNonNull(orderByClauseChain);
    }
}
