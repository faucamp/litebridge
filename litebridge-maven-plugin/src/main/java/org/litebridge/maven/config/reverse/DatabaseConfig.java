package org.litebridge.maven.config.reverse;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.StringJoiner;

/**
 * Database configuration for reverse engineering.
 */
public final class DatabaseConfig {

    /**
     * Litebridge database provider class.
     * <p>
     * Example: {@code org.litebridge.db.h2.H2DatabaseProvider}
     */
    @Parameter(required = true)
    private String databaseProviderClass;

    /**
     * Database connection URL.
     * <p>
     * Example: {@code jdbc:h2:mem:lb}
     */
    @Parameter(required = true)
    private String url;

    /**
     * Database username
     */
    @Parameter(required = true)
    private String user;

    /**
     * Database password
     * <p>
     * Defaults to an empty string.
     */
    @Parameter(defaultValue = "")
    private String password = "";

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
