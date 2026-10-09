# Spring Boot 3 compatibility example

## Overview

This example uses `litebridge-spring-boot-starter` to set up Litebridge in Spring Boot 3 via Spring Boot configuration.

It is the same as `litebridge-example-spring-boot`, but demonstrates compatibility with Spring Boot 3. In addition, it
uses plain DTOs as entities (vs the annotated entities used in `litebridge-example-spring-boot`). This is not a SB3 requirement;
it demonstrates the use of plain DTOs as entities in a Spring environment by using
the `LitebridgeConfig` configurer to register these DTOs during Spring startup.

It uses Litebridge Spring repositories for the DTOs.

## Running the example

1. Start the Spring Boot app by running the `SpringBoot3Application` main class.
2. Access the Swagger page at `http://localhost:8080/swagger-ui/index.html`.
3. Use the Swagger UI to test the Litebridge Spring repositories.