package org.litebridgedb.orm.e2e.lob.entity;

import org.litebridgedb.orm.annotation.AllowInterface;
import org.litebridgedb.orm.annotation.Column;
import org.litebridgedb.orm.annotation.Table;

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
