package org.litebridge.db.spi.math;


/**
 * Predefined mathematical operators for use in SQL operations.
 */
public enum MathOperator {
    /**
     * Addition operator for mathematical calculations.
     * <p>
     * The operator symbol is "+".
     */
    ADD("+"),
    /**
     * Subtraction operator for mathematical calculations.
     * <p>
     * The operator symbol is "-".
     */
    SUBTRACT("-"),
    /**
     * Multiplication operator for mathematical calculations.
     * <p>
     * The operator symbol is "*".
     */
    MULTIPLY("*"),
    /**
     * Division operator for mathematical calculations.
     * <p>
     * The operator symbol is "/".
     */
    DIVIDE("/"),
    /**
     * Modulo operator for mathematical calculations.
     * <p>
     * The operator symbol is "%".
     */
    MOD("%");

    private final String symbol;

    MathOperator(final String symbol) {
        this.symbol = symbol;
    }

    /**
     * Retrieves the symbol representing this operator.
     *
     * @return the operator's symbolic representation as a string.
     */
    public String symbol() {
        return symbol;
    }
}
