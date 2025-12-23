package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.api.select.SelectTerminal;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractSelector<DTO> implements SelectTerminal<DTO> {

    protected final SelectSpec selectSpec;
    protected final DatabaseProvider databaseProvider;
    protected final DtoMapper<DTO> dtoMapper;

    protected AbstractSelector(final SelectSpec selectSpec,
                               final DatabaseProvider databaseProvider,
                               final DtoMapper<DTO> dtoMapper) {
        this.selectSpec = selectSpec;
        this.databaseProvider = databaseProvider;
        this.dtoMapper = dtoMapper;
    }

    protected AbstractSelector(final AbstractSelector<DTO> delegate) {
        this(delegate.selectSpec, delegate.databaseProvider, delegate.dtoMapper);
    }

    @Override
    public Optional<DTO> one() {
        return Optional.ofNullable(oneOrNull());
    }

    @Override
    public @Nullable DTO oneOrNull() {
        return dtoMapper.toDto(fetchOneRecord(false));
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
        return dtoMapper.toDto(fetchOneRecord(true));
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
        return executeQuery().stream().map(dtoMapper::toDto);
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

        final List<Map<String, Object>> resultList = executeQuery();

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
    protected List<Map<String, Object>> executeQuery() {
        // Execute SQL query
        final List<Map<String, Object>> resultList;

        try {
            resultList = databaseProvider.select(selectSpec.toSelect());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute select query", ex);
        }

        return resultList;
    }
}
