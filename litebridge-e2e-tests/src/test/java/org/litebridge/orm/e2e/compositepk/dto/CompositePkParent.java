package org.litebridge.orm.e2e.compositepk.dto;

import org.jspecify.annotations.Nullable;

import java.util.List;

public record CompositePkParent(@Nullable Long pk1,
                                @Nullable Long pk2,
                                String description,
                                @Nullable List<CompositePkChild> children) {
}
