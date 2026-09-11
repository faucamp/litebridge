package org.litebridge.orm.e2e.lob.entity;

import org.litebridge.orm.annotation.AllowInterface;
import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;

@Table("LB.CLOB_TEST")
@AllowInterface(ClobTestEntity.class)
public class DefaultClobTestEntity extends ClobTestEntity {

    @Column("ID")
    @Override
    public Long getId() {
        return super.getId();
    }

    @Column("CLOB_DATA")
    @Override
    public String getData() {
        return super.getData();
    }
}
