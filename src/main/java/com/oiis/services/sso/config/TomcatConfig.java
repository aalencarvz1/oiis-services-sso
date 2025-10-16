package com.oiis.services.sso.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure server to allow run with external port configured on application.yml or .env file and different local port, wich no requires https
 */
@Configuration
public class TomcatConfig {

    @Value("${server.local-port}")
    private Integer LOCAL_PORT;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainer() {
        return server -> {
            Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setPort(LOCAL_PORT);
            server.addAdditionalTomcatConnectors(connector);
        };
    }
}