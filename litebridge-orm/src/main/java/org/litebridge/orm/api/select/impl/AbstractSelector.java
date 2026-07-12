package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.Select;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractSelector<DTO, SSP extends SelectSpec> implements SelectTerminal<DTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSelector.class);
    protected final SSP selectSpec;
    protected final TransactionalDatabaseProvider databaseProvider;
    protected final Class<DTO> dtoClass;
    protected final LitebridgeContext litebridgeContext;

    protected AbstractSelector(final SSP selectSpec,
                               final TransactionalDatabaseProvider databaseProvider,
                               final Class<DTO> dtoClass,
                               final LitebridgeContext litebridgeContext) {
        this.selectSpec = selectSpec;
        this.databaseProvider = databaseProvider;
        this.dtoClass = dtoClass;
        this.litebridgeContext = litebridgeContext;
    }

    protected AbstractSelector(final AbstractSelector<DTO, SSP> delegate) {
        this(delegate.selectSpec, delegate.databaseProvider, delegate.dtoClass, delegate.litebridgeContext);
    }

    @Override
    public Optional<DTO> one() {
        return Optional.<DTO>ofNullable(oneOrNull());
    }

    @Override
    public abstract @Nullable DTO oneOrNull();

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
        return Optional.<DTO>ofNullable(firstOrNull());
    }

    @Override
    public abstract @Nullable DTO firstOrNull();

    @Override
    public DTO firstOrThrow() {
        return firstOrThrow(() -> new NoSuchElementException("No record found for query"));
    }

    @Override
    public <X extends Throwable> DTO firstOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        return first().orElseThrow(exceptionSupplier);
    }

    @Override
    public Stream<DTO> stream() {
        return list().stream();
    }

    @Override
    public abstract List<DTO> list();

    @Override
    public String toSql() {
        return databaseProvider.toSql(selectSpec.toSelect(), databaseProvider.transactionManager());
    }

    protected List<Row> executeQuery() {
        return executeQuery(selectSpec);
    }

    protected List<Row> executeQuery(final SSP selectSpec) {
        // Execute SQL query
        final List<Row> rows;
        final Select select = selectSpec.toSelect();

        try {
            rows = databaseProvider.select(select, databaseProvider.transactionManager());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute select query", ex);
        }

        LOGGER.debug("Row count: {}", rows.size());
        LOGGER.trace("Query result: {}", rows);

        return rows;
    }

    /**
     * Returns the select specification.
     *
     * @return the select specification
     */
    protected SSP selectSpec() {
        return selectSpec;
    }

    public LitebridgeContext litebridgeContext() {
        return litebridgeContext;
    }
}
