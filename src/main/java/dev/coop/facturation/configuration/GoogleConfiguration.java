package dev.coop.facturation.configuration;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.SecurityUtils;
import com.google.api.services.drive.DriveScopes;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Arrays;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 *
 * @author lfo
 */
@Configuration
public class GoogleConfiguration implements EnvironmentAware {

    protected GoogleCredential credential;
    protected String p12Path;
    protected String p12Password;
    protected String serviceAccountId;
    protected Environment environment;
    protected HttpTransport httpTransport;
    protected JacksonFactory jsonFactory;
    protected String[] SCOPES = {"https://spreadsheets.google.com/feeds",
                "https://spreadsheets.google.com/feeds/spreadsheets/private/full",
                "https://docs.google.com/feeds",
                DriveScopes.DRIVE,
                DriveScopes.DRIVE_FILE};
    
    
    @Override
    public void setEnvironment(Environment e) {
        this.environment = e;
        p12Path = environment.getProperty("data.gs.p12Path");
        p12Password = environment.getProperty("data.gs.p12Password");
        serviceAccountId = environment.getProperty("data.gs.serviceAccountId");
    }
    
    @Bean
    public GoogleCredential credential()  {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            jsonFactory = new JacksonFactory();
            credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId(serviceAccountId)
                    .setServiceAccountScopes(Arrays.asList(SCOPES))
                    .setServiceAccountPrivateKey(loadPrivateKey())
//                    .setServiceAccountPrivateKeyFromP12File(new File(p12Path))
                    .build();
            return credential;
        } catch (GeneralSecurityException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected PrivateKey loadPrivateKey() throws GeneralSecurityException, FileNotFoundException, IOException {
        // Cette clé est générée à partir de la console developer google.
        // http://stackoverflow.com/questions/30483601/create-spreadsheet-using-google-spreadsheet-api-in-google-drive-in-java
        // pour voir la liste des spreadsheets ou accéder à un folder,
        // il faut la partager au compte )904143855825-ve04fhlam728q05bps7fpq7qd6av7a3i@developer.gserviceaccount.com) dans le drive.
//        File p12 = new File(p12Path);
//        InputStream input = new FileInputStream(p12);
        byte[] p12 = ByteStreams.toByteArray(this.getClass().getClassLoader().getResourceAsStream(p12Path));
        PrivateKey privateKey =
                SecurityUtils.loadPrivateKeyFromKeyStore(SecurityUtils.getPkcs12KeyStore(), new ByteArrayInputStream(p12), p12Password, "privatekey", p12Password);
        return privateKey;
    }
    
    public String getServiceAccountId() {
        return credential.getServiceAccountId();
    }

    
    public HttpTransport getHttpTransport() {
        return credential.getTransport();
    }

    public JsonFactory getJsonFactory() {
        return credential.getJsonFactory();
    }
    
}
