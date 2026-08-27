package org.litebridge.orm.api.select;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.api.sql.SqlFromClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * Entry point for the "FROM" clause of a query.
 */
public final class FromClauseStart {

    private final String @Nullable [] columns;
    private final ExpressionSpec @Nullable [] expressionSpecs;
    private final SelectEngineTerminal selectEngineTerminal;
    private final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator;

    public FromClauseStart(final ExpressionSpec[] expressionSpecs,
                           final SelectEngineTerminal selectEngineTerminal,
                           final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        this(null, expressionSpecs, selectEngineTerminal, litebridgeContextCreator);
    }

    public FromClauseStart(final String[] columns, final SelectEngineTerminal selectEngineTerminal, final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        this(columns, null, selectEngineTerminal, litebridgeContextCreator);
    }

    public FromClauseStart(final SelectEngineTerminal selectEngineTerminal, final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        this(null, null, selectEngineTerminal, litebridgeContextCreator);
    }

    private FromClauseStart(final String @Nullable [] columns,
                            final ExpressionSpec @Nullable [] expressionSpecs,
                            final SelectEngineTerminal selectEngineTerminal,
                            final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        this.columns = columns;
        this.expressionSpecs = expressionSpecs;
        this.selectEngineTerminal = selectEngineTerminal;
        this.litebridgeContextCreator = litebridgeContextCreator;
    }

    /**
     * Starts a FROM clause for the given DTO class.
     *
     * @param dtoClass the DTO class.
     * @param <DTO>    the DTO type.
     * @return the DTO from clause terminal.
     */
    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass) {
        final SelectNode selectNode = new SelectNode(null, dtoClass, columns, expressionSpecs, null);
        return new DtoFromClauseTerminal<>(selectNode, selectEngineTerminal, litebridgeContextCreator.apply(LitebridgeContext.Mode.DTO));
    }

    /**
     * Starts a FROM clause for the given DTO class within the context of another DTO class.
     *
     * @param dtoClass        the DTO class.
     * @param contextDtoClass the context DTO class.
     * @param <DTO>           the DTO type.
     * @return the DTO from clause terminal.
     */
    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
//        return fromClauseEngine.from(dtoClass, contextDtoClass);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Starts a FROM clause for the given SQL table.
     *
     * @param table the table name.
     * @return the SQL from clause terminal.
     */
    public SqlFromClauseTerminal from(final String table) {
        final SelectNode selectNode = new SelectNode(table, null, columns, expressionSpecs, null);
        return new SqlFromClauseTerminal(selectNode, selectEngineTerminal, litebridgeContextCreator.apply(LitebridgeContext.Mode.SQL));
    }
}