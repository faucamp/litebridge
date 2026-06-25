package org.litebridgedb.orm.api.select.model;

import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.ConvertExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.NestableExpressionSpec;
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

public final class SelectExpressionMapper {

    private final SqlFunctionRegistry sqlFunctionRegistry;

    public SelectExpressionMapper(final SqlFunctionRegistry sqlFunctionRegistry) {
        this.sqlFunctionRegistry = sqlFunctionRegistry;
    }

    SqlFunctionRegistry sqlFunctionRegistry() {
        return sqlFunctionRegistry;
    }

    SelectExpression toSelectExpression(final ExpressionSpec expressionSpec) {
        return switch (expressionSpec) {
            // Select targets
            case SelectFieldSpec selectFieldSpec -> toSelectColumn(selectFieldSpec);
            case SelectColumnSpec selectColumnSpec -> toSelectColumn(selectColumnSpec);
            case SubselectSpec subselectSpec ->
                    sqlFunctionRegistry.select().subselect().create(subselectSpec.selectSpec().toSelect());
            case ConvertSpec<?> convertSpec -> new ConvertExpression(toSelectExpression(convertSpec.target()), convertSpec.returnType());
            case ExpressionSpecArray expressionSpecArray ->
                    throw new IllegalStateException("ExpressionSpecArray not resolved: " + expressionSpecArray);

            // Aggregate functions
            case CountSpec countSpec -> sqlFunctionRegistry.aggregate().count();

            // Nestable expressions
            case NestableExpressionSpec nestableExpression -> resolveNestedExpression(nestableExpression);

            // Date/time
            case CurrentTimestampSpec currentTimestampSpec -> sqlFunctionRegistry.date().currentTimestamp();

            // Unsupported
            case ProtoExpressionSpec protoExpression ->
                    throw new IllegalStateException("ProtoExpression not resolved: " + protoExpression);
        };
    }

    private ColumnExpression resolveNestedExpression(final NestableExpressionSpec expression) {
        final ColumnExpression nestedExpression;

        if (expression.target() instanceof NestableExpressionSpec targetNestableExpression) {
            nestedExpression = resolveNestedExpression(targetNestableExpression);
        } else {
            nestedExpression = (ColumnExpression) toSelectExpression(expression.target());
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

    private ColumnExpression toSelectColumn(final SelectFieldSpec selectFieldSpec) {
        return sqlFunctionRegistry.select().column().create(ObjectUtils.requireNonNull(selectFieldSpec.column(),
                () -> new IllegalStateException("SelectField.column not set")));
    }

    private ColumnExpression toSelectColumn(final SelectColumnSpec selectColumnSpec) {
        return sqlFunctionRegistry.select().column().create(selectColumnSpec.column());
    }
}
