package org.litebridge.example.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ManualConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManualConfigApplication.class, args);
    }
}
