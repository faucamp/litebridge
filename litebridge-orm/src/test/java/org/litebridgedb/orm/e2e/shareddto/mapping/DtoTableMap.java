package org.litebridgedb.orm.e2e.shareddto.mapping;

import org.litebridgedb.orm.api.spec.ColumnMapping;
import org.litebridgedb.orm.api.spec.FieldMapping;
import org.litebridgedb.orm.api.spec.TableSpec;
import org.litebridgedb.orm.e2e.shareddto.dto.Status;

import java.util.Map;

import static org.litebridgedb.orm.api.spec.ColumnMapping.c;
import static org.litebridgedb.orm.api.spec.FieldMapping.f;

public final class DtoTableMap {

    private DtoTableMap() {
    }

    public static final Map<FieldMapping, ColumnMapping> Application = Map.of(
            f("name"), c("NAME"),
            f("status"), c("STATUS_CODE").joinOn("CODE")
                    .withMappedTable(Status.class, new TableSpec("LB.APPLICATION_STATUS", Map.of(
                            f("code"), c("CODE"),
                            f("message"), c("MESSAGE")
                    )))
    );

    public static final Map<FieldMapping, ColumnMapping> Server = Map.of(
            f("host"), c("HOST"),
            f("status"), c("SERVER_STATUS_CODE").joinOn("STATUS_CODE")
                    .withMappedTable(Status.class, new TableSpec("LB.SERVER_STATUS", Map.of(
                            f("code"), c("STATUS_CODE"),
                            f("message"), c("MESSAGE")
                    )))
    );
}
