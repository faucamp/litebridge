package org.litebridge.orm.persistence;

import org.litebridge.db.spi.update.UpdateResult;

import java.util.function.Consumer;

public record PipedStatement(AbstractStatementBuilder<?> statementBuilder, Consumer<UpdateResult> valuePipe) {
}
