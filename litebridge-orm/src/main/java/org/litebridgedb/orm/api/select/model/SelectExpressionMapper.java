package org.litebridgedb.orm.api.select.model;

import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.ConvertExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.DelegateExpressionSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.ProtoExpressionSpec;
import org.litebridgedb.orm.expression.function.aggregate.AvgSpec;
import org.litebridgedb.orm.expression.function.aggregate.CountSpec;
import org.litebridgedb.orm.expression.function.aggregate.MaxSpec;
import org.litebridgedb.orm.expression.function.aggregate.MinSpec;
import org.litebridgedb.orm.expression.function.date.CurrentTimestampSpec;
import org.litebridgedb.orm.expression.function.scalar.AbsSpec;
import org.litebridgedb.orm.expression.function.scalar.LowerSpec;
import org.litebridgedb.orm.expression.function.scalar.SubstringSpec;
import org.litebridgedb.orm.expression.function.scalar.UpperSpec;
import org.litebridgedb.orm.expression.intent.ConvertSpec;
import org.litebridgedb.orm.expression.intent.ExpressionSpecArray;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;
import org.litebridgedb.orm.expression.select.SubselectSpec;
import org.litebridgedb.orm.meta.QueryField;

import java.util.List;

public final class SelectExpressionMapper {

    private final SqlFunctionRegistry sqlFunctionRegistry;
    private final ProtoExpressionResolver protoExpressionResolver;

    public SelectExpressionMapper(final SqlFunctionRegistry sqlFunctionRegistry, final ProtoExpressionResolver protoExpressionResolver) {
        this.sqlFunctionRegistry = sqlFunctionRegistry;
        this.protoExpressionResolver = protoExpressionResolver;
    }

    SqlFunctionRegistry sqlFunctionRegistry() {
        return sqlFunctionRegistry;
    }

    List<ExpressionSpec> resolveProtoExpression(final ExpressionSpec expressionSpec, final ClauseType clause) {
        return protoExpressionResolver.resolveExpression(expressionSpec, clause).toList();
    }

    List<ExpressionSpec> resolveProtoExpressions(final List<ExpressionSpec> expressionSpecs, final ClauseType clause) {
        return protoExpressionResolver.resolveExpressions(expressionSpecs, clause);
    }

    SelectExpression toSelectExpression(final ExpressionSpec expressionSpec, final boolean useSelectReferences) {
        return switch (expressionSpec) {
            // Select targets
            case SelectFieldSpec selectFieldSpec -> toSelectColumn(selectFieldSpec, useSelectReferences);
            case SelectColumnSpec selectColumnSpec -> toSelectColumn(selectColumnSpec, useSelectReferences);
            case SubselectSpec subselectSpec ->
                    sqlFunctionRegistry.select().subselect().create(subselectSpec.selectSpec().toSelect());
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
