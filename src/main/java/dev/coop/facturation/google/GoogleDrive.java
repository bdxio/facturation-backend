package dev.coop.facturation.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import javax.annotation.PostConstruct;
import dev.coop.facturation.configuration.GoogleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author lfo
 */
@Service
public class GoogleDrive {

    @Autowired
    private GoogleConfiguration googleConfiguration;

    private Drive drive;

    @PostConstruct
    public void setup() {
        drive = new Drive.Builder(googleConfiguration.getTransport(), googleConfiguration.getJsonFactory(), googleConfiguration.getHttpRequestInitializer())
                .setApplicationName(GoogleConfiguration.APPLICATION_NAME)
                .build();

    }

    public void upload(String parentId, String title, String mimeType, byte[] bytes) throws IOException {
        File file = new File();
        file.setName(title);
        file.setParents(Collections.singletonList(parentId));
        file.setMimeType(mimeType);
        ByteArrayContent content = new ByteArrayContent(mimeType, bytes);
        Drive.Files.Create create = drive.files().create(file, content).setQuotaUser(UUID.randomUUID().toString());
        MediaHttpUploader uploader = create.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(true);
//        uploader.setProgressListener(new FileUploadProgressListener());
        create.execute();
    }

    public String getFile(String name, String parentId) throws IOException {
//        String pageToken = null;
//        do {
            String q = String.format("'%s' in parents and name = '%s'", parentId, name);
//            System.out.println(q);
            FileList result = drive.files().list()
                    .setQ(q)
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setQuotaUser(UUID.randomUUID().toString())
//                    .setPageToken(pageToken)
                    .execute();
            for (File file : result.getFiles()) {
                return  file.getId();
            }
            return null;
//            pageToken = result.getNextPageToken();
//        } while (pageToken != null);
    }
    
    public void deleteFile(String id) throws IOException {
        drive.files().delete(id).setQuotaUser(UUID.randomUUID().toString()).execute();
    }
}
