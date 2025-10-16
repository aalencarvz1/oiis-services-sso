package com.oiis.services.sso.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manual create flyway bean
 */
@Configuration
public class FlywayAfterHibernate {

    private final String dbUrl;

    private final String dbUser;

    private final String dbPassword;

    public FlywayAfterHibernate(
            @Value("${spring.datasource.sso.jdbc-url}") String dbUrl,
            @Value("${spring.datasource.sso.username}") String dbUser,
            @Value("${spring.datasource.sso.password}") String dbPassword)
    {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

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
            System.out.println("✅ Migrações Flyway runned after Hibernate.");
        };
    }
}