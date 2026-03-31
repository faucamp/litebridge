package org.litebridge.orm.api.delete;

import org.litebridge.db.spi.update.UpdateResult;

public interface DeleteTerminal<DTO> {

    UpdateResult execute();

}
