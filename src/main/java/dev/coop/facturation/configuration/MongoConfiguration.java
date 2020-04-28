package dev.coop.facturation.configuration;

import com.mongodb.Mongo;
import java.time.LocalDate;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.CustomConversions;

/**
 *
 * @author lfo
 */
@Configuration
@Import(value = MongoAutoConfiguration.class)
public class MongoConfiguration extends AbstractMongoConfiguration implements EnvironmentAware {

    @Autowired
    private Mongo mongo;
    private Environment environment;

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(Arrays.asList(new LocalDateToLongConverter(), new LongToLocalDateConverter()));
    }

    @Override
    public Mongo mongo() throws Exception {
        return mongo;
    }

    @Override
    protected String getDatabaseName() {
        return environment.getProperty("data.mongodb.database");
    }

    @Override
    public void setEnvironment(Environment e) {
        this.environment = e;
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
