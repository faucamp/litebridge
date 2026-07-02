package org.litebridgedb.maven.test.entity;

import org.litebridgedb.orm.annotation.Column;
import org.litebridgedb.orm.annotation.Table;

@Table("TEST_RELATED_ENTITY")
public class TestRelatedEntity {

    @Column("ID")
    private Long id;
    @Column("VALUE")
    private int value;
    @Column("ACTIVE")
    private boolean active;
    @Column(value = "TEST_ENTITY_ID", joinOn = "ID")
    private TestEntity testEntity;
}
