package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.tx.ConnectionProvider;

public interface SqlGenerator {

    String generateSql(final Operation operation, final ConnectionProvider connectionProvider);

    MetaDataEngine metaDataEngine();

    SelectSqlGenerator selectSqlGenerator();
}
