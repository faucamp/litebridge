package org.litebridge.orm.expression.select;

import org.litebridge.orm.api.select.SelectApi;
import org.litebridge.orm.api.select.SelectTerminal;

import java.util.function.Function;

public record QueryAliasSpec(Function<SelectApi, SelectTerminal<?>> query, String alias)
        implements SqlFromTargetSpec {
}
