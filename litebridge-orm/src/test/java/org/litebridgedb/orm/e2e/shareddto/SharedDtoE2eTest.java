package org.litebridgedb.orm.e2e.shareddto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridgedb.orm.e2e.AbstractE2eTest;
import org.litebridgedb.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridgedb.orm.e2e.shareddto.dto.Application;
import org.litebridgedb.orm.e2e.shareddto.dto.Server;
import org.litebridgedb.orm.e2e.shareddto.dto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SharedDtoE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SharedDtoE2eTest.class);

    @TestTemplate
    @DisplayName("Persist objects that share a nested DTO mapped to different tables")
    void sharedDto_differentTables(final DbEnvDtoTableMapper tableMapper) {
        // Register DTOs and construct data
        litebridge.register(Application.class, rc -> rc.mapToTable(tableMapper.qualifyName("APPLICATION"))
                .mapField("name").toColumn("NAME")
                .mapField("status").toColumn("STATUS_CODE").joinOn("CODE")
                .withMappedTable(Status.class, src -> src.mapToTable(tableMapper.qualifyName("APPLICATION_STATUS"))
                        .mapField("code").toColumn("CODE")
                        .mapField("message").toColumn("MESSAGE")));

        litebridge.register(Server.class, rc -> rc.mapToTable(tableMapper.qualifyName("SERVER"))
                .mapField("host").toColumn("HOST")
                .mapField("status").toColumn("SERVER_STATUS_CODE").joinOn("STATUS_CODE")
                .withMappedTable(Status.class, src -> src.mapToTable(tableMapper.qualifyName("SERVER_STATUS"))
                        .mapField("code").toColumn("STATUS_CODE")
                        .mapField("message").toColumn("MESSAGE")));

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