package org.litebridge.maven.test.entity;

import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.OneToMany;
import org.litebridge.orm.annotation.Table;

import java.util.List;

@Table("TEST_ENTITY")
public class TestEntity {

    @Column("ID")
    private Long id;
    @Column("NAME")
    private String name;
    @OneToMany(mappedByField = "testEntity")
    private List<TestRelatedEntity> relatedEntities;
}
