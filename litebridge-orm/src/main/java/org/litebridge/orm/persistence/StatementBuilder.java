package org.litebridge.orm.persistence;

import org.litebridge.db.spi.update.UpdateStatement;

public sealed interface StatementBuilder<US extends UpdateStatement> permits AbstractStatementBuilder, NoOpStatementBuilder {

    StatementChain statementChain();

    US build();
}
