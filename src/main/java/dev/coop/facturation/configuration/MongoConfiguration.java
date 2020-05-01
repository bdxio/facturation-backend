package dev.coop.facturation.configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.LocalDate;
import java.util.Arrays;

/**
 *
 * @author lfo
 */
@Configuration
@Import(value = MongoAutoConfiguration.class)
public class MongoConfiguration extends AbstractMongoClientConfiguration implements EnvironmentAware {
    private Environment environment;

    @Override
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(new LocalDateToLongConverter(), new LongToLocalDateConverter()));
    }

    @Override
    protected String getDatabaseName() {
        return environment.getProperty("data.mongodb.database");
    }

    @Override
    public void setEnvironment(Environment e) {
        this.environment = e;
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create();
    }

    public static class LocalDateToLongConverter implements Converter<LocalDate, Long> {

        @Override
        public Long convert(LocalDate arg0) {
            return arg0.toEpochDay();
        }
    }

    public static class LongToLocalDateConverter implements Converter<Long, LocalDate> {

        @Override
        public LocalDate convert(Long arg0) {
            return LocalDate.ofEpochDay(arg0);
        }
    }
}
