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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SharedDtoE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SharedDtoE2eTest.class);

    @TestTemplate
    @DisplayName("Persist objects that share a nested DTO mapped to different tables")
    void sharedDto_differentTables(final DbEnvDtoTableMapper tableMapper) {
        // Set up per-database table/column names
        final String tableApplication = tableMapper.qualifyName("APPLICATION");
        final String tableApplicationStatus = tableMapper.qualifyName("APPLICATION_STATUS");
        final String tableServer = tableMapper.qualifyName("SERVER");
        final String tableServerStatus = tableMapper.qualifyName("SERVER_STATUS");

        final String columnName = tableMapper.transformColumnName("NAME");
        final String columnStatusCode = tableMapper.transformColumnName("STATUS_CODE");
        final String columnCode = tableMapper.transformColumnName("CODE");
        final String columnHost = tableMapper.transformColumnName("HOST");
        final String columnServerStatusCode = tableMapper.transformColumnName("SERVER_STATUS_CODE");
        final String columnMessage = tableMapper.transformColumnName("MESSAGE");

        // Register DTOs and construct data
        litebridge.register(Application.class, rc -> rc.mapToTable(tableApplication)
                .with(spec -> spec.mapField("name").toColumn(columnName))
                .with(spec -> spec.mapField("status").toColumn(columnStatusCode)
                        .joinOn(tableMapper.transformColumnName(columnCode))
                        .withMappedTable(Status.class, src -> src.mapToTable(tableApplicationStatus)
                                .with(s -> s.mapField("code").toColumn(columnCode))
                                .with(s -> s.mapField("message").toColumn(columnMessage)))));

        litebridge.register(Server.class, rc -> rc.mapToTable(tableServer)
                .with(spec -> spec.mapField("host").toColumn(columnHost))
                .with(spec -> spec.mapField("status").toColumn(columnServerStatusCode)
                        .joinOn(tableMapper.transformColumnName(columnStatusCode))
                        .withMappedTable(Status.class, src -> src.mapToTable(tableServerStatus)
                                .with(s -> s.mapField("code").toColumn(columnStatusCode))
                                .with(s -> s.mapField("message").toColumn(columnMessage)))));

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

    @TestTemplate
    @DisplayName("Persist objects that share a nested DTO mapped to different tables at arbitrary depth")
    void sharedDto_arbitraryDepth(final DbEnvDtoTableMapper tableMapper) {
        // Set up per-database table/column names
        final String tableTenantA = tableMapper.qualifyName("TENANT_A");
        final String tableTenantSettingA = tableMapper.qualifyName("TENANT_SETTING_A");
        final String tableAccountA = tableMapper.qualifyName("ACCOUNT_A");
        final String tableAccountSettingA = tableMapper.qualifyName("ACCOUNT_SETTING_A");

        final String columnId = tableMapper.transformColumnName("ID");
        final String columnName = tableMapper.transformColumnName("NAME");
        final String columnSettingId = tableMapper.transformColumnName("SETTING_ID");
        final String columnSettingKey = tableMapper.transformColumnName("SETTING_KEY");
        final String columnSettingValue = tableMapper.transformColumnName("SETTING_VALUE");
        final String columnTenantId = tableMapper.transformColumnName("TENANT_ID");

        // Register DTOs
        litebridge.register(Tenant.class, rc -> rc.mapToTable(tableTenantA)
                .with(spec -> spec.mapField("id").toColumn(columnId))
                .with(spec -> spec.mapField("name").toColumn(columnName))
                .with(spec -> spec.mapField("setting").toColumn(columnSettingId)
                        .joinOn(columnId)
                        .withMappedTable(Setting.class, src -> src.mapToTable(tableTenantSettingA)
                                .with(s1 -> s1.mapField("id").toColumn(columnId))
                                .with(s1 -> s1.mapField("key").toColumn(columnSettingKey))
                                .with(s1 -> s1.mapField("value").toColumn(columnSettingValue))))
                .with(spec -> spec.mapField("accounts").oneToMany(otm -> otm.mappedByField("tenantId"))));

        litebridge.register(Account.class, rc -> rc.mapToTable(tableAccountA)
                .with(spec -> spec.mapField("id").toColumn(columnId))
                .with(spec -> spec.mapField("tenantId").toColumn(columnTenantId))
                .with(spec -> spec.mapField("name").toColumn(columnName))
                .with(spec -> spec.mapField("setting").toColumn(columnSettingId)
                        .joinOn(columnId)
                        .withMappedTable(Setting.class, src -> src.mapToTable(tableAccountSettingA)
                                .with(s3 -> s3.mapField("id").toColumn(columnId))
                                .with(s3 -> s3.mapField("key").toColumn(columnSettingKey))
                                .with(s3 -> s3.mapField("value").toColumn(columnSettingValue)))));

        final Setting tenantSetting = new Setting(1, "theme", "dark");
        final Setting accountSetting = new Setting(2, "notifications", "enabled");
        final Account account = new Account(10, 100, "Main Account", accountSetting);
        final Tenant tenant = new Tenant(100, "My Tenant", tenantSetting, List.of(account));

        // Persist
        litebridge.save(tenant);

        // Load back with joins
        final Tenant result = litebridge.select(Tenant.class)
                .join(Setting.class).on("setting")
                .join(Account.class).on("accounts")
                .join(Setting.class).on("setting")
                .where("id").eq(100)
                .oneOrThrow();

        assertEquals(tenant.name(), result.name());
        assertNotNull(result.setting());
        assertEquals(tenantSetting.value(), result.setting().value());
        assertEquals(1, result.accounts().size());
        assertEquals(account.name(), result.accounts().getFirst().name());
        assertNotNull(result.accounts().getFirst().setting());
        assertEquals(accountSetting.value(), result.accounts().getFirst().setting().value());
    }

    public record Tenant(int id, String name, Setting setting, List<Account> accounts) {
    }

    public record Account(int id, Integer tenantId, String name, Setting setting) {
    }

    public record Setting(int id, String key, String value) {
    }
}