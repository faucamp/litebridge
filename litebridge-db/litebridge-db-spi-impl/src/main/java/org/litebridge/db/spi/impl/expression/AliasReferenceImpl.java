package org.litebridge.db.spi.impl.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.AliasReference;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

public class AliasReferenceImpl extends AbstractAliasedExpression implements AliasReference {

    public AliasReferenceImpl(final String alias, final @Nullable String tableAlias, final LabelGenerator labelGenerator) {
        super(alias, tableAlias, labelGenerator);
    }

    public AliasReferenceImpl(final String alias, final LabelGenerator labelGenerator) {
        this(alias, null, labelGenerator);
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        if (tableAlias != null) {
            return labelGenerator.quoteAlias(tableAlias) + "." + labelGenerator.quoteAlias(alias);
        } else {
            return labelGenerator.quoteAlias(alias);
        }
    }
}
