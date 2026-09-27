package org.litebridge.orm.expression.select;

public sealed interface SqlFromTargetSpec extends FromTargetSpec permits QueryAliasSpec, TableAliasSpec {
}
