package org.litebridge.orm.e2e.compositepk.dto;

import org.jspecify.annotations.Nullable;

public class CompositePkTyped {

    private PrimaryKey id;
    private @Nullable String description;

    public PrimaryKey getId() {
        return id;
    }

    public void setId(final PrimaryKey id) {
        this.id = id;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable final String description) {
        this.description = description;
    }

    public record PrimaryKey(@Nullable Long pk1, @Nullable Long pk2) {
    }
}
