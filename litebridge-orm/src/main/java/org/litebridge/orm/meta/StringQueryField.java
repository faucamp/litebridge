package org.litebridge.orm.meta;

import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;
import org.litebridge.orm.expression.ProtoNestableTOExpr;

/**
 * Metamodel field for querying string-based columns in a type-safe manner.
 * <p>
 * This class extends {@link QueryField} to provide additional behavior specifically for
 * string column operations within a query context.
 * <p>
 * Instances of this class allow performing operations such as converting the value of a
 * string field to uppercase or lowercase.
 */
public final class StringQueryField extends QueryField {

    /**
     * Creates a new {@code StringQueryField} instance for the specified DTO class and field name.
     *
     * @param dtoClass  the DTO class
     * @param fieldName the field name
     */
    public StringQueryField(final Class<?> dtoClass, final String fieldName) {
        super(dtoClass, fieldName);
    }

    /**
     * {@code UPPER()}: Returns the uppercase value of a column's text.
     *
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     */
    public StringQueryField upper() {
        if (pendingExpressionSpec != null) {
            pendingExpressionSpec = Fn.lower(pendingExpressionSpec);
        } else {
            pendingExpressionSpec = Fn.upper(Fn.f(dtoClass, field));
        }

        return this;
    }

    /**
     * {@code LOWER()}: Returns the lowercase value of a column's text.
     *
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     */
    public StringQueryField lower() {
        if (pendingExpressionSpec != null) {
            pendingExpressionSpec = Fn.lower(pendingExpressionSpec);
        } else {
            pendingExpressionSpec = Fn.lower(Fn.field(dtoClass, field));
        }

        return this;
    }

    ExpressionSpec pendingExpressionSpec() {
        return pendingExpressionSpec;
    }
}
