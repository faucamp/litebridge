package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.update.UpdateResult;

public class DtoUpdateResult {

    private Object dto;
    @Nullable
    private UpdateResult updateResult;
    @Nullable
    private final DtoUpdateResult parentResult;

    public DtoUpdateResult(final Object dto, final @Nullable DtoUpdateResult parentResult) {
        this.dto = dto;
        this.parentResult = parentResult;
    }

    public Object getDto() {
        return ObjectUtils.requireNonNull(dto, () -> new IllegalStateException("DTO not set"));
    }

    public void setDto(final Object dto) {
        this.dto = dto;
    }

    public UpdateResult getUpdateResult() {
        return ObjectUtils.requireNonNull(updateResult, () -> new IllegalStateException("Update result not set"));
    }

    public void setUpdateResult(final UpdateResult updateResult) {
        this.updateResult = updateResult;
    }

    public @Nullable DtoUpdateResult getParentResult() {
        return parentResult;
    }
}
