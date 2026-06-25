package org.litebridgedb.db.spi.expression;

/**
 * Abstract base class for nesting query expressions such as SQL functions.
 * <p>
 * This class provides a foundation for creating SQL function expressions
 * by encapsulating a {@code Column} and offering a method to access it.
 * It is designed to be extended by concrete implementations to define
 * specific behaviors and SQL representations.
 * <p>
 * Classes that extend {@code ColumnExpression} are expected to implement
 * the {@code toSql} method from the {@code SelectExpression} interface.
 */
public interface DelegateExpression extends SelectExpression {

    SelectExpression target();

}
