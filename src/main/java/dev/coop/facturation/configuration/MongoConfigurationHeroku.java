package dev.coop.facturation.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;

/**
 *
 * @author lforet
 */
@Configuration
@Profile("heroku")
public class MongoConfigurationHeroku extends MongoConfiguration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public @Bean
    MongoDbFactory mongoDbFactory() throws MongoException {
        String uri = System.getenv(MONGODB_URI);
        logger.info(MONGODB_URI + " : " + uri);

        return new SimpleMongoClientDbFactory(new ConnectionString(MONGODB_URI));
    }
    private static final String MONGODB_URI = "MONGODB_URI";

}
