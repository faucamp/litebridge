package org.litebridgedb.maven.config;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.StringJoiner;

public class DatabaseConfig {

    /**
     * Litebridge database provider class.
     * <p>
     * Example: {@code org.litebridgedb.db.h2.H2DatabaseProvider}
     */
    @Parameter(property = "databaseProviderClass", required = true)
    private String databaseProviderClass;

    /**
     * Database connection URL.
     * <p>
     * Example: {@code jdbc:h2:mem:lb}
     */
    @Parameter(property = "url", required = true)
    private String url;

    /**
     * Database username
     */
    @Parameter(property = "user", required = true)
    private String user;

    /**
     * Database password
     */
    @Parameter(property = "password", required = true)
    private String password;

    public String getDatabaseProviderClass() {
        return databaseProviderClass;
    }

    public void setDatabaseProviderClass(final String databaseProviderClass) {
        this.databaseProviderClass = databaseProviderClass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DatabaseConfig.class.getSimpleName() + "[", "]")
                .add("databaseProviderClass='" + databaseProviderClass + "'")
                .add("url='" + url + "'")
                .add("user='" + user + "'")
                .add("password='***'")
                .toString();
    }
}
