package org.litebridge.orm.persistence;

import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class DelegatingSelectorChain<T> implements SelectorChain<T> {

    protected final Selector<T> selector;

    public DelegatingSelectorChain(final Selector<T> selector) {
        this.selector = selector;
    }

    @Override
    public SelectorChain<T> orderBy(final String column) {
        return selector.orderBy(column);
    }

    @Override
    public SelectorChain<T> offset(final int offset) {
        return selector.offset(offset);
    }

    @Override
    public SelectorChain<T> limit(final int limit) {
        return selector.limit(limit);
    }

    @Override
    public Optional<T> one() {
        return selector.one();
    }

    @Nullable
    @Override
    public T oneOrNull() {
        return selector.oneOrNull();
    }

    @Override
    public T oneOrThrow() {
        return selector.oneOrThrow();
    }

    @Override
    public <X extends Throwable> T oneOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        return selector.oneOrThrow();
    }

    @Override
    public Optional<T> first() {
        return selector.first();
    }

    @Nullable
    @Override
    public T firstOrNull() {
        return selector.firstOrNull();
    }

    @Override
    public T firstOrThrow() {
        return selector.oneOrThrow();
    }

    @Override
    public <X extends Throwable> T firstOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        return selector.firstOrThrow(exceptionSupplier);
    }

    @Override
    public Stream<T> stream() {
        return selector.stream();
    }

    @Override
    public List<T> list() {
        return selector.list();
    }
}
