package org.litebridgedb.orm.e2e.compositepk.dto;

import org.jspecify.annotations.Nullable;

public record CompositePkSimple(@Nullable Long pk1, @Nullable Long pk2, String description) {
}
