package org.litebridge.db.oracle.sql;

import org.litebridge.db.spi.impl.sql.LabelGenerator;

public final class OracleLabelGenerator extends LabelGenerator {

    @Override
    public String createAliasAs(final String alias) {
        return ' ' + quoteAlias(alias);
    }
}
