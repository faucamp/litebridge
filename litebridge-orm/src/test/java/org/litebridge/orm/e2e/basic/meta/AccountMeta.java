package org.litebridge.orm.e2e.basic.meta;

import org.litebridge.orm.e2e.basic.dto.Account;
import org.litebridge.orm.meta.NumericQueryField;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.StringQueryField;

public class AccountMeta {

    public static final NumericQueryField id = new NumericQueryField(Account.class, "id");
    public static final StringQueryField name = new StringQueryField(Account.class, "name");
    public static final NumericQueryField balance = new NumericQueryField(Account.class, "balance");
    public static final QueryField owner = new QueryField(Account.class, "owner");
}
