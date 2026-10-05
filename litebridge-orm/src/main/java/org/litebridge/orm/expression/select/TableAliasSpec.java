package org.litebridge.orm.expression.select;

public record TableAliasSpec(String table, String alias)
        implements SqlFromTargetSpec {
}
