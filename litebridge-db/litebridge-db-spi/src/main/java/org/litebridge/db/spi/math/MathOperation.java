package org.litebridge.db.spi.math;

/**
 * Represents a mathematical operation to be performed on a column in a SQL context.
 * This class encapsulates an operator and a value that defines the operation.
 * <p>
 * The operation is defined using an {@link Operator}, which specifies the type of
 * mathematical operation (e.g., addition, subtraction, multiplication, etc.), and
 * a value that serves as the operand for the operation.
 * <p>
 * Instances of this class are immutable.
 *
 * @param operator The operator to be applied in the mathematical operation.
 *                 Must be one of the predefined {@link Operator} values.
 * @param value    The operand to apply the operation with. Can represent various
 *                 types of data, depending on the context in which the operation is used.
 */
public record MathOperation(Operator operator, Object value) {

    /**
     * Predefined mathematical operators for use in SQL operations.
     */
    public enum Operator {
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

        Operator(final String symbol) {
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
}
