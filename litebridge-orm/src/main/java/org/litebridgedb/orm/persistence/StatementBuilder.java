package org.litebridgedb.orm.persistence;

import org.litebridgedb.db.spi.update.UpdateStatement;

public sealed interface StatementBuilder<US extends UpdateStatement> permits AbstractStatementBuilder, NoOpStatementBuilder {

    StatementChain statementChain();

    US build();
}
