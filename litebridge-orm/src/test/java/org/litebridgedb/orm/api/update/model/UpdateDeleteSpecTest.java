package org.litebridgedb.orm.api.update.model;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.db.spi.update.ColumnValue;
import org.litebridgedb.db.spi.update.Update;
import org.litebridgedb.orm.api.delete.model.DeleteSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;

import static org.junit.jupiter.api.Assertions.*;

class UpdateDeleteSpecTest {

    @Test
    void updateSpec() {
        UpdateSpec spec = new UpdateSpec();
        Table table = new Table("cat", "sch", "tab");
        spec.setTable(table);
        assertEquals(table, spec.getTable());
        
        Column col = new Column(table, "col");
        spec.addColumnValue(new ColumnValue(col, "val"));
        ConditionSpec conditionSpec = spec.newWhereCondition(col);
        conditionSpec.setOperator(Operator.EQ);
        conditionSpec.setValue("test");
        
        assertNotNull(spec.getWhereConditions());
        assertEquals(1, spec.getWhereConditions().size());
        
        Update update = spec.toUpdate();
        assertEquals(table, update.table());
        assertEquals(1, update.columnValues().size());
        assertEquals(1, update.where().size());
        
        spec.setWhereConditions(null);
        assertNull(spec.getWhereConditions());
    }

    @Test
    void deleteSpec() {
        DeleteSpec spec = new DeleteSpec();
        Table table = new Table("cat", "sch", "tab");
        spec.setTable(table);
        assertEquals(table, spec.getTable());
        
        Column col = new Column(table, "col");
        ConditionSpec conditionSpec = spec.newWhereCondition(col);
        conditionSpec.setOperator(Operator.EQ);
        conditionSpec.setValue("test");
        
        assertNotNull(spec.getWhereConditions());
        
        spec.setWhereConditions(null);
        assertNull(spec.getWhereConditions());
        
        spec.setTable(null);
        assertThrows(IllegalStateException.class, spec::getTable);
    }
}
