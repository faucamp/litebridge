# Spring Boot manual configuration

## Overview

This example uses `litebridge-spring` to set up Litebridge in Spring Boot via manual configuration.

It does not use repositories, but rather a facade pattern with Litebridge used directly with plain DTOs as entities.

To use Litebridge repositories in a manual setup, annotate the main application class (or a configuration class) 
with `@EnableLitebridgeAnnotations`, and see `litebridge-example-spring-boot` for a repository implementation example.

## Running the example

1. Start the Spring Boot app by running the `ManualConfigApplication` main class.
2. Access the Swagger page at `http://localhost:8080/swagger-ui/index.html`.
3. Use the Swagger UI to test the Litebridge Spring repositories.