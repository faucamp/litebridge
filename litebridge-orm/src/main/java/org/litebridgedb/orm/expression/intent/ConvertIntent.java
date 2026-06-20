package org.litebridgedb.orm.expression.intent;

import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.ExpressionModifier;
import org.litebridgedb.orm.expression.TypeOverride;

/**
 * Intent to convert a database result into the specified Java type.
 * <p>
 * This uses Litebridge's registered type converter to perform the conversion;
 * it is not a database operation via creation of a {@link ConvertSpec}.
 * <p>
 * This specific intent class is designed to help with a fluent API flow
 * via the {@link org.litebridgedb.orm.Litebridge#select(TypeOverride, ExpressionSpec...)}
 * select API.
 */
public record ConvertIntent<T>(ExpressionSpec target, Class<T> returnType)
        implements ExpressionModifier, TypeOverride<T> {

    public ConvertSpec<T> toExpression() {
        return new ConvertSpec<>(target, returnType);
    }

}
