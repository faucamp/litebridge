package org.litebridge.spring.boot3.test.repository;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;

@Table("SB3_PERSON")
public class SpringBoot3Person {

    @Column("ID")
    private @Nullable Long id;

    @Column("NAME")
    private @Nullable String name;

    @Column("SURNAME")
    private @Nullable String surname;

    @Column("AGE")
    private @Nullable Integer age;

    public @Nullable Long getId() {
        return id;
    }

    public void setId(final @Nullable Long id) {
        this.id = id;
    }

    public @Nullable String getName() {
        return name;
    }

    public void setName(final @Nullable String name) {
        this.name = name;
    }

    public @Nullable String getSurname() {
        return surname;
    }

    public void setSurname(final @Nullable String surname) {
        this.surname = surname;
    }

    public @Nullable Integer getAge() {
        return age;
    }

    public void setAge(final @Nullable Integer age) {
        this.age = age;
    }
}
