package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.orm.api.select.SelectTerminal;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractSelectTerminal<DTO> implements SelectTerminal<DTO> {

    protected final SelectSpec selectSpec;

    public AbstractSelectTerminal(final SelectSpec selectSpec) {
        this.selectSpec = selectSpec;
    }

    @Override
    public Optional<DTO> one() {
        return Optional.ofNullable(oneOrNull());
    }

    @Override
    public @Nullable DTO oneOrNull() {
        return toDto(fetchOneRecord(false));
    }

    @Override
    public DTO oneOrThrow() {
        return oneOrThrow(() -> new NoSuchElementException("No record found for query"));
    }

    @Override
    public <X extends Throwable> DTO oneOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        return one().orElseThrow(exceptionSupplier);
    }

    @Override
    public Optional<DTO> first() {
        return Optional.ofNullable(firstOrNull());
    }

    @Override
    public @Nullable DTO firstOrNull() {
        return toDto(fetchOneRecord(true));
    }

    @Override
    public DTO firstOrThrow() {
        return oneOrThrow(() -> new NoSuchElementException("No record found for query"));
    }

    @Override
    public <X extends Throwable> DTO firstOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        return one().orElseThrow(exceptionSupplier);
    }

    @Override
    public Stream<DTO> stream() {
        return executeQuery().map(this::toDto);
    }

    @Override
    public List<DTO> list() {
        return stream().toList();
    }

    protected @Nullable Map<String, Object> fetchOneRecord(final boolean first) {
        if (first) {
            // Set LIMIT since we are only interested in the first record
            selectSpec.ensureLimit().setLimit(1);
        }

        final List<Map<String, Object>> resultList = executeQuery().toList();

        if (CollectionUtils.isEmpty(resultList)) {
            return null;
        }

        if (!first && resultList.size() > 1) {
            throw new IllegalStateException("Expected exactly one result, but got %d".formatted(resultList.size()));
        }

        return resultList.getFirst();
    }

    /**
     * Executes the query and returns a {@link Stream} of records.
     *
     * @return a {@link Stream} of @{code Map<String, Object>} corresponding to records return from query result.
     */
    protected abstract Stream<Map<String, Object>> executeQuery();

    protected abstract @Nullable DTO toDto(final @Nullable Map<String, Object> row);
}
