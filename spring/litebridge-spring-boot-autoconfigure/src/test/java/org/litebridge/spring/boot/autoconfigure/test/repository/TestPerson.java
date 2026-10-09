package org.litebridge.spring.boot.autoconfigure.test.repository;

import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;

@Table("TEST_PERSON")
public class TestPerson {

    @Column("ID")
    private Long id;

    @Column("NAME")
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
