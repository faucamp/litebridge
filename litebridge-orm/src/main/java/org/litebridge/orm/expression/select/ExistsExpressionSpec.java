package org.litebridge.orm.expression.select;

import org.litebridge.orm.api.select.SelectApi;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.expression.TypeOverrideExpressionSpec;

import java.util.function.Function;

public record ExistsExpressionSpec(
        Function<SelectApi, SelectTerminal<?>> query) implements TypeOverrideExpressionSpec<Boolean> {
    
    @Override
    public Class<Boolean> returnType() {
        return Boolean.class;
    }
}
