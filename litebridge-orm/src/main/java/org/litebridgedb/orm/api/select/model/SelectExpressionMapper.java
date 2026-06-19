package org.litebridgedb.orm.api.select.model;

import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.orm.expression.Expression;
import org.litebridgedb.orm.expression.NestableExpression;
import org.litebridgedb.orm.expression.ProtoExpression;
import org.litebridgedb.orm.expression.function.aggregate.AvgSpec;
import org.litebridgedb.orm.expression.function.aggregate.CountSpec;
import org.litebridgedb.orm.expression.function.aggregate.MaxSpec;
import org.litebridgedb.orm.expression.function.aggregate.MinSpec;
import org.litebridgedb.orm.expression.function.date.CurrentTimestampSpec;
import org.litebridgedb.orm.expression.function.java.ConvertSpec;
import org.litebridgedb.orm.expression.function.scalar.AbsSpec;
import org.litebridgedb.orm.expression.function.scalar.LowerSpec;
import org.litebridgedb.orm.expression.function.scalar.SubstringSpec;
import org.litebridgedb.orm.expression.function.scalar.UpperSpec;
import org.litebridgedb.orm.expression.select.SelectColumn;
import org.litebridgedb.orm.expression.select.SelectField;

final class SelectExpressionMapper {

    private final SqlFunctionRegistry sqlFunctionRegistry;

    SelectExpressionMapper(final SqlFunctionRegistry sqlFunctionRegistry) {
        this.sqlFunctionRegistry = sqlFunctionRegistry;
    }

    SelectExpression toSelectExpression(final Expression expression) {
        return switch (expression) {
            // Select columns
            case SelectField selectField -> toSelectColumn(selectField);
            case SelectColumn selectColumn -> toSelectColumn(selectColumn);

            // Aggregate functions
            case CountSpec countSpec -> sqlFunctionRegistry.aggregate().count();

            // Nestable expressions
            case NestableExpression nestableExpression -> resolveNestedExpression(nestableExpression);

            // Date/time
            case CurrentTimestampSpec currentTimestampSpec -> sqlFunctionRegistry.date().currentTimestamp();

            // Unsupported
            case ConvertSpec<?> convertSpec -> throw new IllegalStateException("ConvertSpec is ORM-side only");
            case ProtoExpression protoExpression ->
                    throw new IllegalStateException("ProtoExpression not resolved: " + protoExpression);
        };
    }

    private ColumnExpression resolveNestedExpression(final NestableExpression expression) {
        final ColumnExpression nestedExpression;

        if (expression.target() instanceof NestableExpression targetNestableExpression) {
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

    ColumnExpression toSelectColumn(final SelectField selectField) {
        return sqlFunctionRegistry.selectColumnFactory()
                .create(ObjectUtils.requireNonNull(selectField.column(),
                        () -> new IllegalStateException("SelectField.column not set")));
    }

    ColumnExpression toSelectColumn(final SelectColumn selectColumn) {
        return sqlFunctionRegistry.selectColumnFactory()
                .create(selectColumn.column());
    }
}
