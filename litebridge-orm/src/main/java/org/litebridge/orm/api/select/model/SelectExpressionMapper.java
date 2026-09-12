package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.ConvertExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.query.Select;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.DelegateExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.function.aggregate.AvgSpec;
import org.litebridge.orm.expression.function.aggregate.CountSpec;
import org.litebridge.orm.expression.function.aggregate.MaxSpec;
import org.litebridge.orm.expression.function.aggregate.MinSpec;
import org.litebridge.orm.expression.function.date.CurrentTimestampSpec;
import org.litebridge.orm.expression.function.scalar.AbsSpec;
import org.litebridge.orm.expression.function.scalar.LowerSpec;
import org.litebridge.orm.expression.function.scalar.SubstringSpec;
import org.litebridge.orm.expression.function.scalar.UpperSpec;
import org.litebridge.orm.expression.intent.ConvertSpec;
import org.litebridge.orm.expression.intent.ExpressionSpecArray;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.util.List;

/**
 * Maps high-level {@link ExpressionSpec} query expressions to dialect-specific {@link SelectExpression} instances.
 */
public final class SelectExpressionMapper {

    private final SqlFunctionRegistry sqlFunctionRegistry;
    private final ProtoExpressionResolver protoExpressionResolver;
    private final TableMetaDataCache tableMetaDataCache;
    private final TypeConverter typeConverter;

    /**
     * Creates a new {@code SelectExpressionMapper} instance.
     *
     * @param sqlFunctionRegistry     the SQL function registry
     * @param protoExpressionResolver the proto expression resolver
     * @param tableMetaDataCache      the table metadata cache
     * @param typeConverter           the type converter
     */
    public SelectExpressionMapper(final SqlFunctionRegistry sqlFunctionRegistry,
                                  final ProtoExpressionResolver protoExpressionResolver,
                                  final TableMetaDataCache tableMetaDataCache,
                                  final TypeConverter typeConverter) {
        this.sqlFunctionRegistry = sqlFunctionRegistry;
        this.protoExpressionResolver = protoExpressionResolver;
        this.tableMetaDataCache = tableMetaDataCache;
        this.typeConverter = typeConverter;
    }

    SqlFunctionRegistry sqlFunctionRegistry() {
        return sqlFunctionRegistry;
    }

    /**
     * Resolves a proto-expression into one or more concrete expression specifications.
     *
     * @param expressionSpec the expression specification to resolve
     * @param ormTable       the ORM table metadata, or {@code null}
     * @param table          the SQL table
     * @param clause         the clause type where the expression is used
     * @return the list of resolved expression specifications
     */
    public List<ExpressionSpec> resolveProtoExpression(final ExpressionSpec expressionSpec, final @Nullable OrmTable ormTable, final Table table, final ClauseType clause) {
        return protoExpressionResolver.resolveExpression(expressionSpec, ormTable, table, clause).toList();
    }

    List<ExpressionSpec> resolveProtoExpressions(final List<ExpressionSpec> expressionSpecs, final @Nullable OrmTable ormTable, final Table table, final ClauseType clause) {
        return protoExpressionResolver.resolveExpressions(expressionSpecs, ormTable, table, clause);
    }

    /**
     * Converts an {@link ExpressionSpec} into a dialect-specific {@link SelectExpression}.
     *
     * @param expressionSpec      the expression specification to convert
     * @param useSelectReferences whether to use column references rather than direct column names
     * @return the converted {@link SelectExpression}
     */
    public SelectExpression toSelectExpression(final ExpressionSpec expressionSpec, final boolean useSelectReferences) {
        return switch (expressionSpec) {
            // Select targets
            case SelectFieldSpec selectFieldSpec -> toSelectColumn(selectFieldSpec, useSelectReferences);
            case SelectColumnSpec selectColumnSpec -> toSelectColumn(selectColumnSpec, useSelectReferences);
            case ConvertSpec<?> convertSpec ->
                    new ConvertExpression(toSelectExpression(convertSpec.target(), useSelectReferences), convertSpec.returnType());
            case ExpressionSpecArray expressionSpecArray ->
                    throw new IllegalStateException("ExpressionSpecArray not resolved: " + expressionSpecArray);

            // Aggregate functions
            case CountSpec countSpec -> sqlFunctionRegistry.aggregate().count();

            // Nestable expressions
            case DelegateExpressionSpec nestableExpression ->
                    resolveNestedExpression(nestableExpression, useSelectReferences);

            // Date/time
            case CurrentTimestampSpec currentTimestampSpec -> sqlFunctionRegistry.date().currentTimestamp();

            // Unsupported
            case ProtoExpressionSpec protoExpression ->
                    throw new IllegalStateException("ProtoExpression not resolved: " + protoExpression);
            case QueryField queryField -> throw new IllegalStateException("QueryField not resolved: " + queryField);
        };
    }

    private ColumnExpression resolveNestedExpression(final DelegateExpressionSpec expression, final boolean useSelectReferences) {
        final ColumnExpression nestedExpression;

        if (expression.target() instanceof DelegateExpressionSpec targetNestableExpression) {
            nestedExpression = resolveNestedExpression(targetNestableExpression, useSelectReferences);
        } else {
            nestedExpression = (ColumnExpression) toSelectExpression(expression.target(), useSelectReferences);
        }

        return switch (expression) {
            // Aggregate functions
            case AvgSpec<?> avgSpec -> sqlFunctionRegistry.aggregate().avg().create(nestedExpression);
            case MaxSpec<?> maxSpec -> sqlFunctionRegistry.aggregate().max().create(nestedExpression);
            case MinSpec<?> minSpec -> sqlFunctionRegistry.aggregate().min().create(nestedExpression);

            // Scalar functions
            case UpperSpec upperSpec -> sqlFunctionRegistry.scalar().upper().create(nestedExpression);
            case LowerSpec lowerSpec -> sqlFunctionRegistry.scalar().lower().create(nestedExpression);
            case SubstringSpec substringSpec ->
                    sqlFunctionRegistry.scalar().substring().create(nestedExpression, substringSpec.start(), substringSpec.length());
            case AbsSpec absSpec -> sqlFunctionRegistry.scalar().abs().create(nestedExpression);
        };
    }

    private ColumnExpression toSelectColumn(final ColumnExpressionSpec columnExpressionSpec, final boolean useSelectReferences) {
        final Column column = ObjectUtils.requireNonNull(columnExpressionSpec.getColumn(), () -> new IllegalStateException("SelectField.column not set"));

        if (useSelectReferences) {
            return sqlFunctionRegistry.select().reference().create(column);
        } else {
            return sqlFunctionRegistry.select().column().create(column);
        }

    }

    private ColumnExpression toSelectReference(final ColumnExpressionSpec selectColumnSpec) {
        return sqlFunctionRegistry.select().reference().create(selectColumnSpec.getColumn());
    }
}
