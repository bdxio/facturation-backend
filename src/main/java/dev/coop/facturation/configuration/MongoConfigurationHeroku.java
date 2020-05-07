package dev.coop.facturation.configuration;

import com.google.common.base.Preconditions;
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
    private static final String MONGODB_URI = "MONGODB_URI";

    private final ConnectionString connectionString;

    public MongoConfigurationHeroku() {
        // mongo uri is pass by env
        String uri = System.getenv(MONGODB_URI);
        logger.info(MONGODB_URI + " : " + uri);

        Preconditions.checkNotNull(uri, String.format("Mongo URI is null, is environment variable %s define ?", MONGODB_URI));

        connectionString = new ConnectionString(uri + "?retryWrites=false");
        Preconditions.checkNotNull(connectionString.getDatabase(), String.format("Mongo URI does not contain a database"));
    }

    @Override
    public @Bean
    MongoDbFactory mongoDbFactory() throws MongoException {
        // Heroku mongo's module pass the mongo uri by env
        String uri = System.getenv(MONGODB_URI);
        logger.info(MONGODB_URI + " : " + uri);

        return new SimpleMongoClientDbFactory(connectionString);
    }

    @Override
    protected String getDatabaseName() {
        return connectionString.getDatabase();
    }
}
