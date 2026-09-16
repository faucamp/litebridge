package org.litebridge.db.spi.impl.function;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.AliasReference;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;

import java.util.StringJoiner;

import static org.litebridge.db.spi.impl.ColumnIdentifierGenerator.quoteIdentifier;

public class AliasReferenceImpl implements AliasReference {

    protected final String alias;
    protected final @Nullable String tableAlias;

    public AliasReferenceImpl(final String alias, final @Nullable String tableAlias) {
        this.alias = alias;
        this.tableAlias = tableAlias;
    }

    public AliasReferenceImpl(final String alias) {
        this(alias, null);
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        if (tableAlias != null) {
            return quoteIdentifier(tableAlias) + "." + quoteIdentifier(alias);
        } else {
            return quoteIdentifier(alias);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AliasReferenceImpl.class.getSimpleName() + "[", "]")
                .add("tableAlias='" + tableAlias + "'")
                .add("alias='" + alias + "'")
                .toString();
    }
}
