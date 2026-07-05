package org.litebridgedb.maven.test.entity;

import org.litebridgedb.orm.annotation.Column;
import org.litebridgedb.orm.annotation.OneToMany;
import org.litebridgedb.orm.annotation.Table;

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
