package dev.coop.facturation.configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

/**
 *
 * @author lfo
 */
@Import(value = MongoAutoConfiguration.class)
public abstract class MongoConfiguration extends AbstractMongoClientConfiguration {
    @Override
    public MongoClient mongoClient() {
        return MongoClients.create();
    }
}
