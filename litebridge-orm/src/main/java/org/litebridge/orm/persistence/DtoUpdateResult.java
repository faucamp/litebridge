package org.litebridge.orm.persistence;

import org.litebridge.db.spi.update.UpdateResult;

public class DtoUpdateResult {

    private final Object dto;
    private final UpdateResult updateResult;

    public DtoUpdateResult(final Object dto, final UpdateResult updateResult) {
        this.dto = dto;
        this.updateResult = updateResult;
    }

    public Object dto() {
        return dto;
    }

    public UpdateResult updateResult() {
        return updateResult;
    }
}
