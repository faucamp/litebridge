package org.litebridge.db.spi.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.engine.ExecutionEngine;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.impl.sql.SqlGenerator;

import java.util.function.Function;

public record DatabaseProviderContext(SqlGenerator sqlGenerator,
                                      MetaDataEngine metaDataEngine,
                                      ExecutionEngine executionEngine,
                                      SqlFunctionRegistry sqlFunctionRegistry,
                                      ColumnIdentifierGenerator columnIdentifierGenerator,
                                      Function<String, SequenceColumnValueGenerator> sequenceColumnValueGeneratorCreator) {
}
