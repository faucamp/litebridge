package org.litebridge.db.spi.expression;

import org.litebridge.db.spi.ColumnType;

/**
 * A bind value in a query expression.
 */
public interface BindValueExpression extends AliasedExpression {

    /**
     * Gets the bind value index.
     *
     * @return The index of the bind value.
     */
    int index();


    /**
     * Gets the length/number of bind values (e.g. for collection expressions).
     *
     * @return The length/size of the bind value.
     */
    int size();

    /**
     * Gets the data type and size of the bind value data type.
     *
     * @return The data type and size of the bind value data type.
     */
    ColumnType columnType();
}
