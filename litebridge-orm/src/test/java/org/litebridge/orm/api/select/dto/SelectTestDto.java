package org.litebridge.orm.api.select.dto;

import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;

@Table("test_users")
public class SelectTestDto {

    @Column("id")
    private Long id;

    @Column("name")
    private String name;

    @Column("age")
    private Integer age;

    public SelectTestDto() {
    }

    public SelectTestDto(final Long id, final String name, final Integer age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }
}
