package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.dto.delete.DtoDeleteSpec;
import org.litebridge.orm.api.dto.update.DtoUpdateSpec;
import org.litebridge.orm.persistence.OrmTable;

public sealed interface DtoDataSpec permits DtoJoinSpec, DtoSelectSpec, DtoDeleteSpec, DtoUpdateSpec {

    OrmTable dtoTable();

}
