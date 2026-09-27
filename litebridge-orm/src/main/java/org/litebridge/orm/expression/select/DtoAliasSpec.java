package org.litebridge.orm.expression.select;

public record DtoAliasSpec<DTO>(Class<DTO> dtoClass, String alias)
        implements FromTargetSpec {
}
