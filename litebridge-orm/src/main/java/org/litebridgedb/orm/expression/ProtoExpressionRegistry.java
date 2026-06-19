package org.litebridgedb.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.type.TriFunction;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.function.aggregate.AvgSpec;
import org.litebridgedb.orm.expression.function.scalar.LowerSpec;
import org.litebridgedb.orm.expression.function.scalar.SubstringSpec;
import org.litebridgedb.orm.expression.function.scalar.UpperSpec;
import org.litebridgedb.orm.expression.select.SelectColumn;
import org.litebridgedb.orm.expression.select.SelectField;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

final class ProtoExpressionRegistry {

    private static final Map<Class<? extends Expression>, Function<Column, Expression>> columnExpressions = Map.of(
            SelectColumn.class, SelectColumn::new,
            SelectField.class, SelectColumn::new);

    private static final Map<Class<? extends Expression>, Function<ColumnExpression, NestableExpression>> nestableColumnExpressions = Map.of(
            UpperSpec.class, UpperSpec::new,
            LowerSpec.class, LowerSpec::new);

    private static final Map<Class<? extends Expression>, BiFunction<ColumnExpression, Class<?>, NestableExpression>> typeOverrideColumnExpressions = Map.of(
            AvgSpec.class, AvgSpec::new);

    private static final Map<Class<? extends Expression>, TriFunction<ColumnExpression, Class<?>, @Nullable Object[], NestableExpression>> argTypeOverrideExpressions = Map.of(
            SubstringSpec.class, (target, type, args) -> new SubstringSpec(target, (int) args[0], (Integer) args[1]));

    static boolean isSupported(final Class<? extends Expression> type) {
        return columnExpressions.containsKey(type)
                || typeOverrideColumnExpressions.containsKey(type)
                || argTypeOverrideExpressions.containsKey(type);
    }

    static Expression resolve(final Class<? extends Expression> type,
                              final Column column,
                              final @Nullable Object @Nullable [] args) {
        return columnExpressions.get(type).apply(column);
    }

    static ColumnExpression resolveNestableExpression(final ProtoNestableExpression expression,
                                                      final Column column) {
        final ProtoExpression nestedExpression = expression.target();
        final ColumnExpression resolvedNestedExpression;

        if (nestedExpression instanceof ProtoNestableExpression targetNestedExpression) {
            resolvedNestedExpression = resolveNestableExpression(targetNestedExpression, column);
        } else if (expression.target() instanceof ProtoColumnExpression protoColumnExpression) {
            resolvedNestedExpression = (ColumnExpression) resolve(protoColumnExpression.type(), column, protoColumnExpression.args());
        } else {
            throw new IllegalStateException("Unsupported expression: " + expression);
        }

        if (expression.args() == null) {
            if (nestableColumnExpressions.containsKey(expression.type())) {
                return nestableColumnExpressions.get(expression.type()).apply(resolvedNestedExpression);
            }

            return typeOverrideColumnExpressions.get(expression.type()).apply(resolvedNestedExpression, expression.type());
        }

        return argTypeOverrideExpressions.get(expression.type()).apply(resolvedNestedExpression, expression.type(), expression.args());
    }
}
