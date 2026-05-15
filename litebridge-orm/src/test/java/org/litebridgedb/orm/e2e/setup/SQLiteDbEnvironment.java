package org.litebridge.orm.e2e.setup;

import org.litebridge.db.sqlite.SQLiteDatabaseProvider;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.orm.tx.LitebridgeDriverManagerDataSource;

import java.sql.Connection;

public class SQLiteDbEnvironment implements DbEnvironment {

    private final String url = "jdbc:sqlite:file:lb?mode=memory&cache=shared";
    private final String user = "";
    private final String password = "";
    private LitebridgeDriverManagerDataSource dataSource;
    private Connection keepAliveConnection;

    @Override
    public void start() throws Exception {
        if (keepAliveConnection == null) {
            keepAliveConnection = getDataSource().getConnection();
        }
    }

    @Override
    public void stop() throws Exception {
        if (keepAliveConnection != null) {
            keepAliveConnection.close();
            keepAliveConnection = null;
        }
        if (dataSource != null) {
            dataSource = null;
        }
    }

    @Override
    public LitebridgeDriverManagerDataSource getDataSource() {
        if (dataSource == null) {
            dataSource = new LitebridgeDriverManagerDataSource(url, user, password);
        }

        return dataSource;
    }

    @Override
    public String[] getMigrationLocations() {
        return new String[]{"classpath:db/migration/sqlite"};
    }

    @Override
    public String getName() {
        return "SQLite";
    }

    @Override
    public String getJdbcUrl() {
        return url;
    }

    @Override
    public String getUsername() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public DatabaseProvider getDatabaseProvider() {
        return new SQLiteDatabaseProvider();
    }
}
