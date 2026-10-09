package org.litebridge.spring.boot3.test.repository;

import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;

@Table("SB3_PERSON")
public class SpringBoot3Person {

    @Column("ID")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
}
