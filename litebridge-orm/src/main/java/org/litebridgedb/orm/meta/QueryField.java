package org.litebridgedb.orm.meta;

import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.Fn;
import org.litebridgedb.orm.expression.ProtoColumnExpressionSpec;
import org.litebridgedb.orm.expression.intent.ConvertSpec;

/**
 * Basic metamodel field definition.
 * <p>
 * Entity/DTO metamodels are used for type-safe queries. This implementation provides no additional
 * functionality beyond the ability to be used in queries.
 */
public sealed class QueryField implements ExpressionSpec permits NumericQueryField, StringQueryField {

    /**
     * Class of the DTO the field belongs to
     */
    protected final Class<?> dtoClass;
    /**
     * Target field name.
     */
    protected final String field;

    /**
     * Creates a new {@link QueryField} instance.
     *
     * @param dtoClass DTO class the field belongs to.
     * @param field    Target field name.
     */
    public QueryField(final Class<?> dtoClass, final String field) {
        this.dtoClass = dtoClass;
        this.field = field;
    }

    /**
     * Converts the value of the database column targeted by this field into the specified Java type.
     * <p>
     * This uses Litebridge's registered type converter to perform the conversion;
     * it is not a database operation.
     * <p>
     *
     * @param returnType The type to convert the expression result to
     * @return a {@link ProtoColumnExpressionSpec} expression instance to convert the return value of the nested expression
     */
    public <T> ConvertSpec<T> convert(final Class<T> returnType) {
        return Fn.convert(Fn.f(field), returnType);
    }

    /**
     * Returns the DTO class the field belongs to.
     *
     * @return DTO class.
     */
    public Class<?> dtoClass() {
        return dtoClass;
    }

    /**
     * Returns the name of the field.
     *
     * @return Field name.
     */
    String field() {
        return field;
    }

    @Override
    public String toString() {
        return field;
    }
}
