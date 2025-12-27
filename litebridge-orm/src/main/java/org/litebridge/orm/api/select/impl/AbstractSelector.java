package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.DtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractSelector<DTO> implements SelectTerminal<DTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSelector.class);
    protected final SelectSpec selectSpec;
    protected final DatabaseProvider databaseProvider;
    protected final DtoMapper dtoMapper;
    protected final Class<DTO> dtoClass;

    protected AbstractSelector(final SelectSpec selectSpec,
                               final DatabaseProvider databaseProvider,
                               final DtoMapper dtoMapper, final Class<DTO> dtoClass) {
        this.selectSpec = selectSpec;
        this.databaseProvider = databaseProvider;
        this.dtoMapper = dtoMapper;
        this.dtoClass = dtoClass;
    }

    protected AbstractSelector(final AbstractSelector<DTO> delegate) {
        this(delegate.selectSpec, delegate.databaseProvider, delegate.dtoMapper, delegate.dtoClass);
    }

    @Override
    public Optional<DTO> one() {
        return Optional.ofNullable(oneOrNull());
    }

    @Override
    public @Nullable DTO oneOrNull() {
        return dtoMapper.toDto(fetchOneRecord(false), dtoClass);
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
        return dtoMapper.toDto(fetchOneRecord(true), dtoClass);
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
        return executeQuery().stream().map(row -> dtoMapper.toDto(row, dtoClass));
    }

    @Override
    public List<DTO> list() {
        return stream().toList();
    }

    protected @Nullable Row fetchOneRecord(final boolean first) {
        if (first) {
            // Set LIMIT since we are only interested in the first record
            selectSpec.ensureLimit().setLimit(1);
        }

        final List<Row> resultList = executeQuery();

        if (CollectionUtils.isEmpty(resultList)) {
            return null;
        }

        if (!first && resultList.size() > 1) {
            throw new IllegalStateException("Expected exactly one result, but got %d".formatted(resultList.size()));
        }

        return resultList.getFirst();
    }

    protected List<Row> executeQuery() {
        return executeQuery(selectSpec);
    }

    protected List<Row> executeQuery(final SelectSpec selectSpec) {
        // Execute SQL query
        final List<Row> rows;

        try {
            rows = databaseProvider.select(selectSpec.toSelect());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute select query", ex);
        }

        LOGGER.debug("Row count: {}", rows.size());
        LOGGER.trace("Query result: {}", rows);
        return rows;
    }

    protected final SelectSpec selectSpec() {
        return selectSpec;
    }
}
