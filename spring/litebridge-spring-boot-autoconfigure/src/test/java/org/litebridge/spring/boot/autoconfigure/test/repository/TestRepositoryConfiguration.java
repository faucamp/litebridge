package org.litebridge.spring.boot.autoconfigure.test.repository;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@AutoConfigurationPackage
public class TestRepositoryConfiguration {

    @Bean
    public DataSource dataSource() {
        return new DriverManagerDataSource("jdbc:h2:mem:repodb;DB_CLOSE_DELAY=-1");
    }
}
