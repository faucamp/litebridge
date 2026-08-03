package org.litebridge.orm.api.select.impl;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractJoinSpec extends AbstractConditionBasedSpec implements JoinSpec {

    public AbstractJoinSpec(final Table table, final SelectExpressionMapper selectExpressionMapper) {
        super(table, selectExpressionMapper);
    }

    @Override
    public ConditionSpec using(final String column) {
        final Column spiColumn = new Column(table, column);
        final ConditionSpec usingCondition = conditions.newCondition(LogicOperator.NOOP, new SelectColumnSpec(spiColumn));
        usingCondition.setOperator(Operator.USING);
        return usingCondition;
    }

    @Override
    public Join toJoin(final Collection<Table> availableTables,
                       final List<BindValue> bindValues,
                       final TableMetaDataCache tableMetaDataCache,
                       final TypeConverter typeConverter) {
        final Set<Table> resolutionTables = new HashSet<>(availableTables);
        resolutionTables.add(table);
        return new Join(table, conditions.toConditionGroup(selectExpressionMapper, resolutionTables, bindValues, tableMetaDataCache, typeConverter));
    }
}
