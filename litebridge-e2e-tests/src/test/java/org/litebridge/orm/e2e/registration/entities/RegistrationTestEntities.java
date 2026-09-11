package org.litebridge.orm.e2e.registration.entities;

import org.litebridge.orm.annotation.AllowInterface;
import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;

public final class RegistrationTestEntities {
    private RegistrationTestEntities() {
    }

    public static class EntityBase {

        @Column("ID")
        private Long id;
        @Column("VAL")
        private String value;
    }

    @Table("LB.E0")
    public static final class E0 extends EntityBase {
    }

    @Table("LB.E1")
    public static final class E1 extends EntityBase {

        @Column(value = "E0_ID", joinOn = "ID")
        private E0 other;
    }

    @Table("LB.E2")
    public static final class E2 extends EntityBase {

        @Column(value = "E1_ID", joinOn = "ID")
        private E1 other;
    }

    @Table("LB.E3")
    public static final class E3 extends EntityBase {

        @Column(value = "E2_ID", joinOn = "ID")
        private E2 other;
        @Column(value = "E4_ID", joinOn = "ID")
        private E4 other2;
    }

    @Table("LB.E4")
    public static final class E4 extends EntityBase {

        @Column(value = "E5_ID", joinOn = "ID")
        private E5 other;
    }

    @Table("LB.E5")
    public static final class E5 extends EntityBase {

        @Column(value = "E4_ID", joinOn = "ID")
        private E4 other;
    }

    @Table("LB.E6")
    @AllowInterface(E6Interface.class)
    public static final class E6 extends EntityBase implements E6Interface {

    }

    @Table("LB.E7")
    public static final class E7 extends EntityBase {
        @Column(value = "E6_ID", joinOn = "ID")
        private E6Interface other;
    }

    @Table("LB.E8")
    public static final class E8 extends EntityBase {
    }

    @Table("LB.E9")
    public static final class E9 extends EntityBase {

        @Column(value = "E8_ID", joinOn = "ID")
        private E8 other;
    }

    public interface E6Interface {

    }
}
