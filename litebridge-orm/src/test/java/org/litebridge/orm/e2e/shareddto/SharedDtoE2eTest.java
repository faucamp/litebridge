package org.litebridge.orm.e2e.shareddto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.shareddto.dto.Application;
import org.litebridge.orm.e2e.shareddto.dto.Server;
import org.litebridge.orm.e2e.shareddto.dto.Status;
import org.litebridge.orm.e2e.shareddto.mapping.DtoTableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.litebridge.orm.api.spec.TableSpec.t;

class SharedDtoE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SharedDtoE2eTest.class);

    @Test
    @DisplayName("Persist objects that share a nested DTO mapped to different tables")
    void sharedDto_differentTables() throws Exception {
        // Register DTOs and construct data
        litebridge.register(Application.class, t("LB.APPLICATION", DtoTableMap.Application));
        litebridge.register(Server.class, t("LB.SERVER", DtoTableMap.Server));

        final Application application = new Application();
        application.setName("MyApp");
        application.setStatus(new Status(200, "OK"));

        final Server server = new Server();
        server.setHost("localhost");
        server.setStatus(new Status(418, "I'm a teapot"));

        // Persist DTOs
        litebridge.save(application);
        litebridge.save(server);

        // Load back DTOs
        final Application resultApplication = litebridge.select(Application.class)
                .join(Status.class).on("status")
                .oneOrThrow();
        assertEquals(application.getName(), resultApplication.getName());
        assertNotNull(application.getStatus());
        assertEquals(application.getStatus().code(), resultApplication.getStatus().code());
        assertEquals(application.getStatus().message(), resultApplication.getStatus().message());

        final Server resultServer = litebridge.select(Server.class)
                .join(Status.class).on("status")
                .oneOrThrow();
        assertEquals(server.getHost(), resultServer.getHost());
        assertNotNull(server.getStatus());
        assertEquals(server.getStatus().code(), resultServer.getStatus().code());
        assertEquals(server.getStatus().message(), resultServer.getStatus().message());

        // Load specific Status object
        final Status resultStatus = litebridge.select(Status.class, Server.class)
                .where("code").eq(418)
                .oneOrThrow();

        assertEquals(server.getStatus().code(), resultStatus.code());
        assertEquals(server.getStatus().message(), resultStatus.message());
    }
}