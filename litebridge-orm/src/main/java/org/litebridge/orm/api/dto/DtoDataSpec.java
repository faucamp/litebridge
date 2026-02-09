package org.litebridge.orm.api.dto;

import org.litebridge.orm.persistence.OrmTable;

public sealed interface DtoDataSpec permits DtoSelectSpec, DtoJoinSpec{

    OrmTable dtoTable();

}
