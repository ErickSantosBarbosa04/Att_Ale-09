package br.com.jurishome.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;

@Configuration(proxyBeanMethods = false)
@EnableMongoHttpSession(collectionName = "sessions", maxInactiveIntervalInSeconds = 1800)
public class MongoSessionConfig {
    // As sessões HTTP, inclusive o SecurityContext, são persistidas no MongoDB.
}
