package org.litebridge.orm.e2e.shareddto.mapping;

import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.FieldMapping;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.e2e.shareddto.dto.Status;

import java.util.Map;

import static org.litebridge.orm.api.spec.ColumnMapping.c;
import static org.litebridge.orm.api.spec.FieldMapping.f;
import static org.litebridge.orm.api.spec.TableSpec.t;

public final class DtoTableMap {

    private DtoTableMap() {
    }

    public static final Map<FieldMapping, ColumnMapping> Application = Map.of(
            f("name"), c("NAME"),
            f("status"), c("STATUS_CODE").joinOn("CODE")
                    .withMappedTable(Status.class, t("LB.APPLICATION_STATUS", Map.of(
                            f("code"), c("CODE"),
                            f("message"), c("MESSAGE")
                    )))
    );

    public static final Map<FieldMapping, ColumnMapping> Server = Map.of(
            f("host"), c("HOST"),
            f("status"), c("SERVER_STATUS_CODE").joinOn("STATUS_CODE")
                    .withMappedTable(Status.class, t("LB.SERVER_STATUS", Map.of(
                            f("code"), c("STATUS_CODE"),
                            f("message"), c("MESSAGE")
                    )))
    );
}
