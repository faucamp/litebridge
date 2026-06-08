package org.litebridgedb.example.spring.config;

import org.litebridgedb.example.common.mapping.CommonDtoRegistration;
import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.spring.boot.autoconfigure.LitebridgeConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LitebridgeConfig implements LitebridgeConfigurer {

    @Override
    public void configure(final Litebridge litebridge) {
        CommonDtoRegistration.registerPersonAndAccount(litebridge);
    }
}
