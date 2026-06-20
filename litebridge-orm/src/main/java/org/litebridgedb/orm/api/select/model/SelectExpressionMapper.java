package org.litebridgedb.orm.api.select.model;

import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.db.spi.expression.ColumnExpression;
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
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;

final class SelectExpressionMapper {

    private final SqlFunctionRegistry sqlFunctionRegistry;

    SelectExpressionMapper(final SqlFunctionRegistry sqlFunctionRegistry) {
        this.sqlFunctionRegistry = sqlFunctionRegistry;
    }

    SelectExpression toSelectExpression(final ExpressionSpec expressionSpec) {
        return switch (expressionSpec) {
            // Select columns
            case SelectFieldSpec selectFieldSpec -> toSelectColumn(selectFieldSpec);
            case SelectColumnSpec selectColumnSpec -> toSelectColumn(selectColumnSpec);

            // Aggregate functions
            case CountSpec countSpec -> sqlFunctionRegistry.aggregate().count();

            // Nestable expressions
            case NestableExpressionSpec nestableExpression -> resolveNestedExpression(nestableExpression);

            // Date/time
            case CurrentTimestampSpec currentTimestampSpec -> sqlFunctionRegistry.date().currentTimestamp();

            // Unsupported
            case ConvertSpec convertSpec -> throw new IllegalStateException("ConvertSpec is ORM-side only");
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

    ColumnExpression toSelectColumn(final SelectFieldSpec selectFieldSpec) {
        return sqlFunctionRegistry.selectColumnFactory()
                .create(ObjectUtils.requireNonNull(selectFieldSpec.column(),
                        () -> new IllegalStateException("SelectField.column not set")));
    }

    ColumnExpression toSelectColumn(final SelectColumnSpec selectColumnSpec) {
        return sqlFunctionRegistry.selectColumnFactory()
                .create(selectColumnSpec.column());
    }
}
