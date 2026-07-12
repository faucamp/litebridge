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

    /**
     * Creates a new DtoUpdateResult.
     *
     * @param dto          the updated DTO
     * @param parentResult the parent update result, or null if none
     */
    public DtoUpdateResult(final Object dto, final @Nullable DtoUpdateResult parentResult) {
        this.dto = dto;
        this.parentResult = parentResult;
    }

    /**
     * Returns the updated DTO.
     *
     * @return the updated DTO
     */
    public Object getDto() {
        return ObjectUtils.requireNonNull(dto, () -> new IllegalStateException("DTO not set"));
    }

    /**
     * Sets the updated DTO.
     *
     * @param dto the updated DTO
     */
    public void setDto(final Object dto) {
        this.dto = dto;
    }

    /**
     * Returns the underlying update result.
     *
     * @return the update result
     */
    public UpdateResult getUpdateResult() {
        return ObjectUtils.requireNonNull(updateResult, () -> new IllegalStateException("Update result not set"));
    }

    /**
     * Sets the underlying update result.
     *
     * @param updateResult the update result
     */
    public void setUpdateResult(final UpdateResult updateResult) {
        this.updateResult = updateResult;
    }

    /**
     * Returns the parent update result.
     *
     * @return the parent result, or null if none
     */
    public @Nullable DtoUpdateResult getParentResult() {
        return parentResult;
    }
}
