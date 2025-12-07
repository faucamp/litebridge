package org.litebridge.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class DbTableChangeTrackingDto extends ChangeTrackingDto {

    @JsonIgnore
    public abstract String getDbTableName();
}
