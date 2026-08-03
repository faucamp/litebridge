package org.litebridge.orm.api.delete.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.orm.api.select.impl.AbstractConditionBasedSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base specification for constructing a SQL DELETE statement.
 */
public class DeleteSpec extends AbstractConditionBasedSpec {

    public DeleteSpec(final Table table, final SelectExpressionMapper selectExpressionMapper) {
        super(table, selectExpressionMapper);
    }

    public PreparedOperation toDelete(final TableMetaDataCache tableMetaDataCache, final TypeConverter typeConverter) {
        final List<BindValue> bindValues = new ArrayList<>();
        final Delete delete = new Delete(table, conditions.toConditionGroup(selectExpressionMapper, Collections.singleton(table), bindValues, tableMetaDataCache, typeConverter));
        return new PreparedOperation(delete, bindValues);
    }
}
