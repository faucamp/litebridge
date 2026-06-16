package org.litebridgedb.orm.function;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Table;

public sealed interface ProtoExpression extends Expression permits ProtoColumnExpression, ProtoTOColumnExpression {

    String column();

    @Nullable String alias();

    Class<? extends Expression> type();

    default Expression resolve(final Table table) {
        return ProtoExpressionRegistry.resolve(type(), table, column(), alias());
    }
}
