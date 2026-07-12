package org.litebridge.orm.api.register;

/**
 * A terminal step in the fluent API for configuring field-to-database-column mappings within
 * an Object-Relational Mapping (ORM) system. This sealed interface represents the final stage
 * in the configuration process, marking the completion of the specification for a database column
 * or join condition associated with a field.
 * <p>
 * This interface is part of a step-by-step builder pattern, ensuring that field-to-column
 * mappings are constructed in a structured and valid sequence. Implementations of this interface
 * provide concrete functionality for finalizing the configuration.
 * <p>
 * The following implementations are permitted:
 * - {@link FieldColumnSpecBuilderTerminalImpl}: Used to directly build a finalized
 * {@code FieldColumnSpec}.
 * - {@link FieldColumnSpecBuilderColumnStep}: Allows additional intermediate configuration
 * for field-to-column mappings.
 * - {@link FieldColumnSpecBuilderJoinStep}: Supports the specification of join conditions
 * for field-to-database-column mappings.
 * <p>
 * This interface acts as a common contract for all terminal steps in the configuration process,
 * providing the final entry point to the orchestration of ORM mapping definitions.
 */
public sealed interface FieldColumnSpecBuilderTerminal
        permits FieldColumnSpecBuilderTerminalImpl,
        FieldColumnSpecBuilderColumnStep,
        FieldColumnSpecBuilderJoinStep {
}
