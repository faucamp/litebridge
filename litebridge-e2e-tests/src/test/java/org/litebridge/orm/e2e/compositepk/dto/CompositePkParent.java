package org.litebridge.orm.e2e.compositepk.dto;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public record CompositePkParent(@Nullable Long pk1,
                                @Nullable Long pk2,
                                String description,
                                @Nullable List<CompositePkChild> children) {

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final CompositePkParent that)) return false;
        return Objects.equals(pk1, that.pk1) && Objects.equals(pk2, that.pk2) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pk1, pk2, description);
    }

    @Override
    public String toString() {
        return "CompositePkParent[pk1=" + pk1 + ", pk2=" + pk2 + ", description=" + description + ", childrenCount=" + (children != null ? children.size() : 0) + "]";
    }
}
