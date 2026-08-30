package org.litebridge.orm.engine;

import org.litebridge.db.spi.query.UpdateMetaData;

import java.util.Collections;

abstract sealed class AbstractUpdateEngine permits DeleteEngine, UpdateEngine {

    protected static final UpdateMetaData UPDATE_META_DATA = new UpdateMetaData(false, Collections.emptyList(), new String[0]);

}
