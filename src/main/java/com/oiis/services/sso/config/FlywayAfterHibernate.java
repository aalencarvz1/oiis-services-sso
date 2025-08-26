package com.oiis.services.sso.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayAfterHibernate {
    // Criar manualmente o bean do Flyway

    //@Value("${spring.datasource.sso.jdbc-url}")
    private final String dbUrl = "jdbc:mysql://127.0.0.1:3306/oiis_sso_dev_v1";

    //@Value("${spring.datasource.sso.user}")
    private final String dbUser = "root";

    //@Value("${spring.datasource.sso.password}")
    private final String dbPassword = "masterkey";

    @Bean
    public Flyway flyway() {
        return Flyway.configure()
                .dataSource(
                        dbUrl,
                        dbUser,
                        dbPassword
                )
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
    }
    @Bean
    public ApplicationRunner runFlywayAfterHibernate(Flyway flyway) {
        return args -> {
            flyway.migrate();
            System.out.println("✅ Migrações Flyway executadas após o Hibernate.");
        };
    }
}