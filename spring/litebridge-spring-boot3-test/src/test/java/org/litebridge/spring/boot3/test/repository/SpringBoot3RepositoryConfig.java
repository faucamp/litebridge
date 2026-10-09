package org.litebridge.spring.boot3.test.repository;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@AutoConfigurationPackage
public class SpringBoot3RepositoryConfig {

    @Bean
    public DataSource dataSource() {
        return new DriverManagerDataSource("jdbc:h2:mem:sb3repodb;DB_CLOSE_DELAY=-1");
    }
}
