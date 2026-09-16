package org.litebridge.orm.expression.select;

import org.litebridge.orm.api.select.SelectApi;
import org.litebridge.orm.api.select.SelectTerminal;

import java.util.function.Function;

public record QueryAliasSpec(String alias,
                             Function<SelectApi, SelectTerminal<?>> query)
        implements FromTargetSpec {
}
