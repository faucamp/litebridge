package org.litebridge.orm.e2e.shareddto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridge.orm.e2e.shareddto.dto.Application;
import org.litebridge.orm.e2e.shareddto.dto.Server;
import org.litebridge.orm.e2e.shareddto.dto.Status;
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
                .with(spec -> spec.mapField("name").toColumn(tableMapper.transformColumnName("NAME")))
                .with(spec -> spec.mapField("status").toColumn(tableMapper.transformColumnName("STATUS_CODE"))
                        .joinOn(tableMapper.transformColumnName(tableMapper.transformColumnName("CODE")))
                        .withMappedTable(Status.class, src -> src.mapToTable(tableMapper.qualifyName("APPLICATION_STATUS"))
                                .with(s -> s.mapField("code").toColumn(tableMapper.transformColumnName("CODE")))
                                .with(s -> s.mapField("message").toColumn(tableMapper.transformColumnName("MESSAGE"))))));

        litebridge.register(Server.class, rc -> rc.mapToTable(tableMapper.qualifyName("SERVER"))
                .with(spec -> spec.mapField("host").toColumn(tableMapper.transformColumnName("HOST")))
                .with(spec -> spec.mapField("status").toColumn(tableMapper.transformColumnName("SERVER_STATUS_CODE"))
                        .joinOn(tableMapper.transformColumnName(tableMapper.transformColumnName("STATUS_CODE")))
                        .withMappedTable(Status.class, src -> src.mapToTable(tableMapper.qualifyName("SERVER_STATUS"))
                                .with(s -> s.mapField("code").toColumn(tableMapper.transformColumnName("STATUS_CODE")))
                                .with(s -> s.mapField("message").toColumn(tableMapper.transformColumnName("MESSAGE"))))));

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
        assertNotNull(resultApplication.getStatus());
        assertEquals(application.getStatus().code(), resultApplication.getStatus().code());
        assertEquals(application.getStatus().message(), resultApplication.getStatus().message());

        final Server resultServer = litebridge.select(Server.class)
                .join(Status.class).on("status")
                .oneOrThrow();
        assertEquals(server.getHost(), resultServer.getHost());
        assertNotNull(resultServer.getStatus());
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