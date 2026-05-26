package org.litebridgedb.orm.api.register;

public sealed interface FieldColumnSpecBuilderTerminal
        permits FieldColumnSpecBuilderTerminalImpl,
        FieldColumnSpecBuilderColumnStep,
FieldColumnSpecBuilderJoinStep{
}
