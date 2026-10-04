package org.litebridge.orm.e2e.compositepk.dto;

import org.jspecify.annotations.Nullable;

public record CompositePkChild(@Nullable Long pk1,
                               @Nullable Long pk2,
                               String description,
                               @Nullable CompositePkParent parent) {
}
