package org.litebridgedb.orm.e2e.basic.mapping;

import org.litebridgedb.orm.api.spec.FieldSpec;

import static org.litebridgedb.orm.api.spec.FieldMapping.f;

/**
 * Simplified mapping reference class
 * <p>
 * Not directly registered with Litebridge; used in {@link org.litebridgedb.orm.e2e.basic.TypeSafeBasicE2eTest}
 */
public class PersonAccountMapping {

    public static final FieldSpec id = f("id");
    public static final FieldSpec name = f("name");
    public static final FieldSpec surname = f("surname");
    public static final FieldSpec age = f("age");
    public static final FieldSpec accountId = f("accountId");
    public static final FieldSpec accountName = f("accountName");
    public static final FieldSpec accountBalance = f("accountBalance");

}
