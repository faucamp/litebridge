package org.litebridgedb.orm.e2e.setup;

import org.litebridgedb.db.h2.H2DatabaseProvider;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.orm.tx.LitebridgeDriverManagerDataSource;

import java.sql.Connection;
import java.sql.Statement;

public class H2DbEnvironment implements DbEnvironment {

    private final String url = "jdbc:h2:mem:lb;DB_CLOSE_DELAY=-1";
    private final String user = "sa";
    private final String password = "";
    private LitebridgeDriverManagerDataSource dataSource;

    @Override
    public void start() {
    }

    @Override
    public void stop() throws Exception {
        if (dataSource != null) {
            final Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("SHUTDOWN");
            connection.close();
            dataSource = null;
        }
    }

    /**
     * Creates an H2 in-memory database connection.
     *
     * @return H2 database connection
     */
    @Override
    public LitebridgeDriverManagerDataSource getDataSource() {
        if (dataSource == null) {
            dataSource = new LitebridgeDriverManagerDataSource(url, user, password);
        }

        return dataSource;
    }

    @Override
    public String[] getMigrationLocations() {
        return new String[]{"classpath:db/migration/common", "classpath:db/migration/h2"};
    }

    @Override
    public String getName() {
        return "H2";
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
        return new H2DatabaseProvider();
    }
}
