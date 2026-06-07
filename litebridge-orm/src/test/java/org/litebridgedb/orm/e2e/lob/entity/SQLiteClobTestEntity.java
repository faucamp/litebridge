package org.litebridgedb.orm.e2e.lob.entity;

import org.litebridgedb.orm.annotation.AllowInterface;
import org.litebridgedb.orm.annotation.Column;
import org.litebridgedb.orm.annotation.Table;

@Table("CLOB_TEST")
@AllowInterface(ClobTestEntity.class)
public class SQLiteClobTestEntity extends ClobTestEntity {

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
