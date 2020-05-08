package dev.coop.facturation.configuration;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableSet;
import dev.coop.facturation.google.GsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Base64Utils;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Set;

/**
 * @author lfo
 */
@Configuration
public class GoogleConfiguration {
    public static final String APPLICATION_NAME = "facturation-backend";

    private static final Set<String> SCOPES = ImmutableSet.of(
            SheetsScopes.SPREADSHEETS_READONLY,
            DriveScopes.DRIVE,
            DriveScopes.DRIVE_FILE
    );

    private final Environment environment;

    private JacksonFactory jsonFactory;
    private NetHttpTransport transport;
    private HttpCredentialsAdapter httpRequestInitializer;

    @Autowired
    public GoogleConfiguration(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void setup() throws GeneralSecurityException, IOException {
        jsonFactory = JacksonFactory.getDefaultInstance();
        transport = GoogleNetHttpTransport.newTrustedTransport();
        final GoogleCredentials googleCredentials = buildCredentials();
        httpRequestInitializer = new HttpCredentialsAdapter(googleCredentials);
    }

    private GoogleCredentials buildCredentials() {
        try {
            final String googleAccount = environment.getRequiredProperty("google.account");
            final byte[] decodedGoogleAccount = Base64Utils.decodeFromString(googleAccount);
            final InputStream inputStream = new ByteArrayInputStream(decodedGoogleAccount);
            return GoogleCredentials.fromStream(inputStream)
                    .createScoped(SCOPES);
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
