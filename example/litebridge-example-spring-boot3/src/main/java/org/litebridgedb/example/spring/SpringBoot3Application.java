package org.litebridgedb.example.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class SpringBoot3Application {

    public static void main(String[] args) {
        SpringApplication.run(SpringBoot3Application.class, args);
    }
}
