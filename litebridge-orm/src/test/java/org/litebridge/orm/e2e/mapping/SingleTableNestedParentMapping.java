package org.litebridge.orm.e2e.mapping;

import org.litebridge.orm.api.spec.FieldColumnSpec;

import static org.litebridge.orm.api.spec.FieldSpecBuilder.f;

public class SingleTableNestedParentMapping {

    public static final FieldColumnSpec parentValue1 = f("parentValue1").c("PARENT_VALUE1");

    public static class NestedChildMapping {
        public static final FieldColumnSpec childValue1 = f("childValue1").c("CHILD_VALUE1");

        public static class NestedGrandChildMapping {
            public static final FieldColumnSpec grandChildValue1 = f("grandChildValue1").c("GRANDCHILD_VALUE1");
        }
    }

}
