package org.litebridge.orm.e2e.compositepk.dto;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class CompositePkTypedClass {

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

    public static class PrimaryKey {
        private @Nullable Long pk1;
        private @Nullable Long pk2;

        public PrimaryKey() {
        }

        public PrimaryKey(final @Nullable Long pk1, final @Nullable Long pk2) {
            this.pk1 = pk1;
            this.pk2 = pk2;
        }

        public @Nullable Long getPk1() {
            return pk1;
        }

        public void setPk1(final @Nullable Long pk1) {
            this.pk1 = pk1;
        }

        public @Nullable Long getPk2() {
            return pk2;
        }

        public void setPk2(final @Nullable Long pk2) {
            this.pk2 = pk2;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof PrimaryKey that)) return false;
            return Objects.equals(pk1, that.pk1) && Objects.equals(pk2, that.pk2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pk1, pk2);
        }
    }
}
