package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.dto.delete.DtoDeleteSpec;
import org.litebridge.orm.persistence.OrmTable;

/**
 * Specification for DTO-based data.
 */
public sealed interface DtoDataSpec permits DtoJoinSpec, DtoSelectSpec, DtoDeleteSpec {

    /**
     * Returns the ORM table metadata for the DTO.
     *
     * @return the ORM table metadata
     */
    OrmTable dtoTable();

}
