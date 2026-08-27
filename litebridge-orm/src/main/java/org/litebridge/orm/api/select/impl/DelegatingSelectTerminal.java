package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.exception.NonUniqueResultException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class DelegatingSelectTerminal<DTO> implements SelectTerminal<DTO> {

    protected final SelectEngineTerminal selectEngineTerminal;
    protected final LitebridgeContext litebridgeContext;
    protected QueryNode node;
    protected @Nullable Supplier<QueryNode> pendingNode;

    protected DelegatingSelectTerminal(final QueryNode node,
                                       final SelectEngineTerminal selectEngineTerminal,
                                       final LitebridgeContext litebridgeContext) {
        this.node = node;
        this.selectEngineTerminal = selectEngineTerminal;
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public Optional<DTO> one() {
        return selectEngineTerminal.fetchOne(node(), litebridgeContext);
    }

    @Override
    public @Nullable DTO oneOrNull() throws NonUniqueResultException {
        return selectEngineTerminal.fetchOneOrNull(node(), litebridgeContext);
    }

    @Override
    public DTO oneOrThrow() throws NoSuchElementException {
        return selectEngineTerminal.fetchOneOrThrow(node(), litebridgeContext);
    }

    @Override
    public <X extends Throwable> DTO oneOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        return selectEngineTerminal.fetchOneOrThrow(node(), litebridgeContext, exceptionSupplier);
    }

    @Override
    public Optional<DTO> first() {
        return selectEngineTerminal.fetchFirst(node(), litebridgeContext);
    }

    @Override
    public @Nullable DTO firstOrNull() {
        return selectEngineTerminal.fetchFirstOrNull(node(), litebridgeContext);
    }

    @Override
    public DTO firstOrThrow() throws NoSuchElementException {
        return selectEngineTerminal.fetchFirstOrThrow(node(), litebridgeContext);
    }

    @Override
    public <X extends Throwable> DTO firstOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        return selectEngineTerminal.fetchFirstOrThrow(node(), litebridgeContext, exceptionSupplier);
    }

    @Override
    public Stream<DTO> stream() {
        return selectEngineTerminal.fetchStream(node(), litebridgeContext);
    }

    @Override
    public List<DTO> list() {
        return selectEngineTerminal.fetchList(node(), litebridgeContext);
    }

    @Override
    public PreparedSql toSql() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    QueryNode node() {
        if (pendingNode != null) {
            return pendingNode.get();
        } else {
            return node;
        }
    }
}
