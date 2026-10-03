package org.litebridge.orm.expression.select;

public sealed interface FromTargetSpec permits DtoAliasSpec, SqlFromTargetSpec, ValuesSpec {
}
