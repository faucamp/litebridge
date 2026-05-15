package org.litebridgedb.db.spi.math;

public record MathOperation(Operator operator, Object value) {

    public enum Operator {
        ADD("+"),
        SUBTRACT("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        MOD("%");

        private final String symbol;

        Operator(final String symbol) {
            this.symbol = symbol;
        }

        public String symbol() {
            return symbol;
        }
    }
}
