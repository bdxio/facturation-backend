package dev.coop.facturation.configuration;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile("default")
public class MongoConfigurationDefault extends MongoConfiguration implements EnvironmentAware {
    private Environment environment;

    @Override
    protected String getDatabaseName() {
        return environment.getProperty("data.mongodb.database");
    }

    @Override
    public void setEnvironment(Environment e) {
        this.environment = e;
    }
}
