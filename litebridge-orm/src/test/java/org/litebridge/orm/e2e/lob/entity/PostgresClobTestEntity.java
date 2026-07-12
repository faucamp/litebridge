package org.litebridge.orm.e2e.lob.entity;

import org.litebridge.orm.annotation.AllowInterface;
import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;

@Table("lb.clob_test")
@AllowInterface(ClobTestEntity.class)
public class PostgresClobTestEntity extends ClobTestEntity {

    @Column("id")
    @Override
    public Long getId() {
        return super.getId();
    }

    @Column("clob_data")
    @Override
    public String getData() {
        return super.getData();
    }
}
