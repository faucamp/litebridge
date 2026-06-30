package org.litebridgedb.orm.meta;

import org.litebridgedb.orm.expression.Fn;
import org.litebridgedb.orm.expression.ProtoNestableTOExpr;

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

    public StringQueryField(final Class<?> dtoClass, final String fieldName) {
        super(dtoClass, fieldName);
    }

    /**
     * {@code UPPER()}: Returns the uppercase value of a column's text.
     *
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     */
    public ProtoNestableTOExpr<String> upper() {
        return Fn.upper(Fn.f(dtoClass, field));
    }

    /**
     * {@code LOWER()}: Returns the lowercase value of a column's text.
     *
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     */
    public ProtoNestableTOExpr<String> lower() {
        return Fn.lower(Fn.f(dtoClass, field));
    }
}
