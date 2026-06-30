package org.litebridgedb.orm.e2e.basic.meta;

import org.litebridgedb.orm.e2e.basic.dto.Person;
import org.litebridgedb.orm.meta.NumericQueryField;
import org.litebridgedb.orm.meta.QueryField;
import org.litebridgedb.orm.meta.StringQueryField;

public class PersonMeta {

    public static final StringQueryField id = new StringQueryField(Person.class, "id");
    public static final StringQueryField name = new StringQueryField(Person.class, "name");
    public static final StringQueryField surname = new StringQueryField(Person.class, "surname");
    public static final NumericQueryField age = new NumericQueryField(Person.class, "age");
    public static final StringQueryField eyeColour = new StringQueryField(Person.class, "eyeColour");
    public static final QueryField accounts = new QueryField(Person.class, "accounts");
    public static final QueryField addresses = new QueryField(Person.class, "addresses");
}
