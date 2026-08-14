package org.litebridge.orm.api.select.model;

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
import org.litebridge.orm.expression.select.SubselectSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.util.List;

public final class SelectExpressionMapper {

    private final SqlFunctionRegistry sqlFunctionRegistry;
    private final ProtoExpressionResolver protoExpressionResolver;
    private final TableMetaDataCache tableMetaDataCache;
    private final TypeConverter typeConverter;

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

    List<ExpressionSpec> resolveProtoExpression(final ExpressionSpec expressionSpec, final Table table, final ClauseType clause) {
        return protoExpressionResolver.resolveExpression(expressionSpec, table, clause).toList();
    }

    List<ExpressionSpec> resolveProtoExpressions(final List<ExpressionSpec> expressionSpecs, final Table table, final ClauseType clause) {
        return protoExpressionResolver.resolveExpressions(expressionSpecs, table, clause);
    }

    SelectExpression toSelectExpression(final ExpressionSpec expressionSpec, final boolean useSelectReferences) {
        return switch (expressionSpec) {
            // Select targets
            case SelectFieldSpec selectFieldSpec -> toSelectColumn(selectFieldSpec, useSelectReferences);
            case SelectColumnSpec selectColumnSpec -> toSelectColumn(selectColumnSpec, useSelectReferences);
            case SubselectSpec subselectSpec -> {
                final PreparedOperation preparedOperation = subselectSpec.selectSpec().toSelect(tableMetaDataCache, typeConverter);
                yield sqlFunctionRegistry.select().subselect().create((Select) preparedOperation.operation());
            }
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
