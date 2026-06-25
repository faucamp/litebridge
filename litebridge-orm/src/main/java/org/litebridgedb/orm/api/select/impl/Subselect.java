package org.litebridgedb.orm.api.select.impl;

import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.engine.SelectEngine;

public class Subselect extends SelectEngine {

    public Subselect(final FromClauseEngine fromClauseEngine) {
        super(fromClauseEngine);
    }
}
