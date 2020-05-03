package dev.coop.facturation.configuration;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import dev.coop.facturation.google.GsException;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Set;

/**
 * @author lfo
 */
@Configuration
public class GoogleConfiguration implements EnvironmentAware {
    public static final String APPLICATION_NAME = "facturation-backend";

    protected GoogleCredentials googleCredentials;
    protected String keyPath;
    protected Environment environment;

    protected JacksonFactory jsonFactory;
    protected NetHttpTransport transport;
    protected HttpCredentialsAdapter httpRequestInitializer;
    protected final Set<String> scopes = Set.of(
            SheetsScopes.SPREADSHEETS_READONLY,
            DriveScopes.DRIVE,
            DriveScopes.DRIVE_FILE
    );

    @PostConstruct
    public void setup() throws GeneralSecurityException, IOException {
        jsonFactory = JacksonFactory.getDefaultInstance();
        transport = GoogleNetHttpTransport.newTrustedTransport();
        googleCredentials = buildCredential();
        httpRequestInitializer = new HttpCredentialsAdapter(googleCredentials);
    }

    @Override
    public void setEnvironment(Environment e) {
        this.environment = e;
        keyPath = environment.getProperty("data.gs.keyPath");
    }

    public GoogleCredentials buildCredential() {
        InputStream in = GoogleConfiguration.class.getClassLoader().getResourceAsStream(keyPath);
        try {
            if (in == null) {
                throw new FileNotFoundException(String.format("Resource not found: %s", keyPath));
            }

            return googleCredentials = GoogleCredentials.fromStream(in)
                    .createScoped(scopes);
        } catch (IOException e) {
            throw new GsException(e);
        }
    }

    public JacksonFactory getJsonFactory() {
        return jsonFactory;
    }

    public NetHttpTransport getTransport() {
        return transport;
    }

    public HttpCredentialsAdapter getHttpRequestInitializer() {
        return httpRequestInitializer;
    }
}
