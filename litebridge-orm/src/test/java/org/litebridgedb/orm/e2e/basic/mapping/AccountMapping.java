package org.litebridgedb.orm.e2e.basic.mapping;

import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.e2e.basic.dto.Account;

public final class AccountMapping extends TypeSafeDtoTableMapping {

    public static final FieldColumnSpec id = field(rc -> rc.mapField("id").toColumn("ACCOUNT_ID").generateUsingSequence("LB.ACCOUNT_SEQ"));
    public static final FieldColumnSpec name = field(rc -> rc.mapField("name").toColumn("ACCOUNT_NAME"));
    public static final FieldColumnSpec balance = field(rc -> rc.mapField("balance").toColumn("BALANCE"));
    public static final FieldColumnSpec owner = field(rc -> rc.mapField("owner").toColumn("PERSON_ID").joinUsing());

    @Override
    protected String table() {
        return "LB.ACCOUNT";
    }

    @Override
    protected Class<?> dtoClass() {
        return Account.class;
    }
}
