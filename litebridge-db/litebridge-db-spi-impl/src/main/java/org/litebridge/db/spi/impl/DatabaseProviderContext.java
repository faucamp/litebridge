package org.litebridge.db.spi.impl;

import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.engine.ExecutionEngine;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.SqlGenerator;

import java.util.function.Function;

public record DatabaseProviderContext(SqlGenerator sqlGenerator,
                                      LabelGenerator labelGenerator,
                                      MetaDataEngine metaDataEngine,
                                      ExecutionEngine executionEngine,
                                      SqlFunctionRegistry sqlFunctionRegistry,
                                      Function<String, SequenceColumnValueGenerator> sequenceColumnValueGeneratorCreator) {
}
