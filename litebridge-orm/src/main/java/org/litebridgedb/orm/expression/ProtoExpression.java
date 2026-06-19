package org.litebridgedb.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;

public sealed interface ProtoExpression extends Expression permits ProtoColumnExpression, ProtoNestableExpression {

    String column();

    @Nullable String alias();

    Class<? extends Expression> type();

    /**
     * Gets any extra expression-specific arguments.
     *
     * @return the extra arguments, or {@code null} if none
     */
    @Nullable Object @Nullable [] args();

    Expression resolve(Table table);

    Expression resolve(Column table);
}
