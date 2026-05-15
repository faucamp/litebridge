package org.litebridgedb.orm.api.dto;

import org.litebridgedb.orm.api.dto.delete.DtoDeleteSpec;
import org.litebridgedb.orm.api.dto.update.DtoUpdateSpec;
import org.litebridgedb.orm.persistence.OrmTable;

public sealed interface DtoDataSpec permits DtoJoinSpec, DtoSelectSpec, DtoDeleteSpec, DtoUpdateSpec {

    OrmTable dtoTable();

}
