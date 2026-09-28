package org.litebridge.example.spring.config;

import org.jspecify.annotations.NonNull;
import org.litebridge.example.common.mapping.CommonDtoRegistration;
import org.litebridge.orm.Litebridge;
import org.litebridge.spring.boot.autoconfigure.LitebridgeConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LitebridgeConfig implements LitebridgeConfigurer {

    @Override
    public void configure(final @NonNull Litebridge litebridge) {
        CommonDtoRegistration.registerPersonAndAccount(litebridge);
    }
}
