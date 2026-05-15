package org.litebridgedb.example.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class AutoConfigurationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoConfigurationApplication.class, args);
    }
}
