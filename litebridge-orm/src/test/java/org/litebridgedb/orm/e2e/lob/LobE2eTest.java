package org.litebridgedb.orm.e2e.lob;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.platform.commons.util.ReflectionUtils;
import org.litebridgedb.orm.e2e.AbstractE2eTest;
import org.litebridgedb.orm.e2e.lob.entity.BlobTestEntity;
import org.litebridgedb.orm.e2e.lob.entity.ClobTestEntity;
import org.litebridgedb.orm.e2e.lob.entity.DefaultBlobTestEntity;
import org.litebridgedb.orm.e2e.lob.entity.DefaultClobTestEntity;
import org.litebridgedb.orm.e2e.lob.entity.PostgresBlobTestEntity;
import org.litebridgedb.orm.e2e.lob.entity.PostgresClobTestEntity;
import org.litebridgedb.orm.e2e.lob.entity.SQLiteBlobTestEntity;
import org.litebridgedb.orm.e2e.lob.entity.SQLiteClobTestEntity;
import org.litebridgedb.orm.e2e.setup.DbEnvDtoTableMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LobE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LobE2eTest.class);

    @TestTemplate
    @DisplayName("BLOB-byte[] test")
    void testBlob(final DbEnvDtoTableMapper tableMapper) throws Exception {
        final Class<? extends BlobTestEntity> blobTestEntityClass = blobTestEntityClassForDbEnv();
        litebridge.register(blobTestEntityClass);

        final byte[] data = "Hello World!".getBytes();
        final BlobTestEntity blobTestEntity = ReflectionUtils.newInstance(blobTestEntityClass);
        blobTestEntity.setId(1L);
        blobTestEntity.setData(data);

        litebridge.save(blobTestEntity);

        final BlobTestEntity result = litebridge.select(BlobTestEntity.class).where("id").eq(1L).oneOrThrow();
        assertEquals("Hello World!", new String(result.getData(), StandardCharsets.UTF_8));
    }

    @TestTemplate
    @DisplayName("CLOB-String test")
    void testClob(final DbEnvDtoTableMapper tableMapper) throws Exception {
        final Class<? extends ClobTestEntity> clobTestEntityClass = clobTestEntityClassForDbEnv();
        litebridge.register(clobTestEntityClass);

        final ClobTestEntity clobTestEntity = ReflectionUtils.newInstance(clobTestEntityClass);
        clobTestEntity.setId(1L);
        clobTestEntity.setData("Hello World!");

        litebridge.save(clobTestEntity);

        final ClobTestEntity result = litebridge.select(ClobTestEntity.class).where("id").eq(1L).oneOrThrow();
        assertEquals("Hello World!", result.getData());
    }

    private Class<? extends BlobTestEntity> blobTestEntityClassForDbEnv() {
        return switch (dbEnv.getName()) {
            case "PostgreSQL" -> PostgresBlobTestEntity.class;
            case "SQLite" -> SQLiteBlobTestEntity.class;
            default -> DefaultBlobTestEntity.class;
        };
    }

    private Class<? extends ClobTestEntity> clobTestEntityClassForDbEnv() {
        return switch (dbEnv.getName()) {
            case "PostgreSQL" -> PostgresClobTestEntity.class;
            case "SQLite" -> SQLiteClobTestEntity.class;
            default -> DefaultClobTestEntity.class;
        };
    }
}