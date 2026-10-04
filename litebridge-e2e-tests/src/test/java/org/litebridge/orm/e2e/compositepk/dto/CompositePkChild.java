package org.litebridge.orm.e2e.compositepk.dto;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public record CompositePkChild(@Nullable Long pk1,
                               @Nullable Long pk2,
                               String description,
                               @Nullable CompositePkParent parent) {

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final CompositePkChild that)) return false;
        return Objects.equals(pk1, that.pk1) && Objects.equals(pk2, that.pk2) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pk1, pk2, description);
    }

    @Override
    public String toString() {
        return "CompositePkChild[pk1=" + pk1 + ", pk2=" + pk2 + ", description=" + description + ", parent=" + (parent != null ? "CompositePkParent[" + parent.pk1() + "," + parent.pk2() + "]" : "null") + "]";
    }
}
