package org.litebridge.orm.persistence;

import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface SelectorChain<T> {

    SelectorChain<T> orderBy(final String column);

    SelectorChain<T> offset(final int offset);

    SelectorChain<T> limit(final int limit);

    Optional<T> one();

    @Nullable
    T oneOrNull();

    T oneOrThrow();

    <X extends Throwable> T oneOrThrow(final Supplier<? extends X> exceptionSupplier) throws X;

    Optional<T> first();

    @Nullable
    T firstOrNull();

    T firstOrThrow();

    <X extends Throwable> T firstOrThrow(final Supplier<? extends X> exceptionSupplier) throws X;

    Stream<T> stream();

    List<T> list();
}
