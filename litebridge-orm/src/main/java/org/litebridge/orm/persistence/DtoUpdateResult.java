package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.update.UpdateResult;

/**
 * Represents the result of an update operation on a Data Transfer Object (DTO).
 * <p>
 * This class encapsulates information about the updated DTO, the outcome
 * of the update operation, and optionally, a parent result if nested updates occurred.
 */
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
