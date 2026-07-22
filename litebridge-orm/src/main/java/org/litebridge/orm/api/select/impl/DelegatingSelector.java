package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.exception.NonUniqueResultException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DelegatingSelector<DTO, SSP extends SelectSpec> implements SelectTerminal<DTO> {

    protected final AbstractSelector<DTO, SSP> delegate;

    public DelegatingSelector(final AbstractSelector<DTO, SSP> delegate) {
        this.delegate = delegate;
    }

    public AbstractSelector<DTO, SSP> delegate() {
        return delegate;
    }

    @Override
    public Optional<DTO> one() {
        return delegate.one();
    }

    @Override
    public @Nullable DTO oneOrNull() throws NonUniqueResultException {
        return delegate.oneOrNull();
    }

    @Override
    public DTO oneOrThrow() throws NoSuchElementException {
        return delegate.oneOrThrow();
    }

    @Override
    public <X extends Throwable> DTO oneOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        return delegate.oneOrThrow(exceptionSupplier);
    }

    @Override
    public Optional<DTO> first() {
        return delegate.first();
    }

    @Override
    public @Nullable DTO firstOrNull() {
        return delegate.firstOrNull();
    }

    @Override
    public DTO firstOrThrow() throws NoSuchElementException {
        return delegate.firstOrThrow();
    }

    @Override
    public <X extends Throwable> DTO firstOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        return delegate.firstOrThrow(exceptionSupplier);
    }

    @Override
    public Stream<DTO> stream() {
        return delegate.stream();
    }

    @Override
    public List<DTO> list() {
        return delegate.list();
    }

    @Override
    public String toSql() {
        return delegate.toSql();
    }
}
