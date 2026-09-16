package org.litebridge.orm.api.select;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.api.select.sql.SqlFromClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.TypeOverrideExpressionSpec;
import org.litebridge.orm.expression.select.FromTargetSpec;
import org.litebridge.orm.expression.select.QueryAliasSpec;

import java.util.Objects;
import java.util.function.Function;

/**
 * Entry point for the "FROM" clause of a query.
 */
public final class FromClauseStart {

    private final String @Nullable [] columns;
    private final ExpressionSpec @Nullable [] expressionSpecs;
    private final SelectEngineTerminal selectEngineTerminal;
    private final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator;

    /**
     * Creates a new {@code FromClauseStart} instance with expression specifications.
     *
     * @param expressionSpecs          the expression specifications
     * @param selectEngineTerminal     the terminal select engine
     * @param litebridgeContextCreator the context creator function
     */
    public FromClauseStart(final ExpressionSpec[] expressionSpecs,
                           final SelectEngineTerminal selectEngineTerminal,
                           final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        this(null, expressionSpecs, selectEngineTerminal, litebridgeContextCreator);
    }

    /**
     * Creates a new {@code FromClauseStart} instance with column names.
     *
     * @param columns                  the column names
     * @param selectEngineTerminal     the terminal select engine
     * @param litebridgeContextCreator the context creator function
     */
    public FromClauseStart(final String[] columns, final SelectEngineTerminal selectEngineTerminal, final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        this(columns, null, selectEngineTerminal, litebridgeContextCreator);
    }

    /**
     * Creates a new {@code FromClauseStart} instance.
     *
     * @param selectEngineTerminal     the terminal select engine
     * @param litebridgeContextCreator the context creator function
     */
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
        final SelectNode selectNode = new SelectNode(dtoClass, null, null, columns, expressionSpecs, null);
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
        final SelectNode selectNode = new SelectNode(dtoClass, contextDtoClass, null, columns, expressionSpecs, null);
        return new DtoFromClauseTerminal<>(selectNode, selectEngineTerminal, litebridgeContextCreator.apply(LitebridgeContext.Mode.DTO));
    }

    /**
     * Starts a FROM clause for the given SQL table.
     *
     * @param table the table name.
     * @return the SQL from clause terminal.
     */
    public SqlFromClauseTerminal from(final String table) {
        final Class<?>[] resultTypes = createResultTypes();
        final SelectNode selectNode = new SelectNode(table, null, columns, expressionSpecs, resultTypes);
        return new SqlFromClauseTerminal(selectNode, selectEngineTerminal, litebridgeContextCreator.apply(LitebridgeContext.Mode.SQL));
    }

    public SqlFromClauseTerminal from(final FromTargetSpec fromTargetSpec) {
        if (fromTargetSpec instanceof QueryAliasSpec queryAliasSpec) {
            return fromImpl(queryAliasSpec.query(), queryAliasSpec.alias());
        } else {
            throw new UnsupportedOperationException("Unsupported from target spec: " + fromTargetSpec);
        }
    }

    /**
     * Specifies a subquery to use as the merge source.
     *
     * @param query function building the subquery
     * @return the merge ON condition clause terminal
     */
    public SqlFromClauseTerminal from(final Function<SelectApi, SelectTerminal<?>> query) {
        return fromImpl(query, null);
    }

    private SqlFromClauseTerminal fromImpl(final Function<SelectApi, SelectTerminal<?>> query, final @Nullable String alias) {
        final Class<?>[] resultTypes = createResultTypes();
        final LitebridgeContext litebridgeContext = litebridgeContextCreator.apply(LitebridgeContext.Mode.SQL);
        final SelectTerminal<?> selectTerminal = query.apply(new SelectApiImpl(litebridgeContext));
        final QueryNode fromQueryTerminalNode = Objects.requireNonNull(SelectTerminalInspector.getNode(selectTerminal));
        final SelectNode selectNode = new SelectNode(fromQueryTerminalNode, alias, columns, expressionSpecs, resultTypes);
        return new SqlFromClauseTerminal(selectNode, selectEngineTerminal, litebridgeContext);
    }

    private Class<?> @Nullable [] createResultTypes() {
        Class<?>[] resultTypes = null;

        if (expressionSpecs != null) {
            for (int i = 0; i < expressionSpecs.length; i++) {
                if (expressionSpecs[i] instanceof TypeOverrideExpressionSpec<?> typeOverrideExpression) {
                    if (resultTypes == null) {
                        resultTypes = new Class<?>[expressionSpecs.length];
                    }

                    resultTypes[i] = typeOverrideExpression.returnType();
                }
            }
        }

        return resultTypes;
    }
}