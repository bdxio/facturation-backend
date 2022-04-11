package dev.coop.facturation.google;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import dev.coop.facturation.configuration.GoogleConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 *
 * @author lfo
 */
@Slf4j
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
        Drive.Files.Create create = drive.files()
                .create(file, content)
                .setQuotaUser(UUID.randomUUID().toString())
                .setSupportsTeamDrives(true);
        MediaHttpUploader uploader = create.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(true);

        create.execute();
    }

    public String getFile(String name, String parentId) throws IOException {
            String q = String.format("'%s' in parents and name = '%s' and trashed = false", parentId, name);
            FileList result = drive.files().list()
                    .setQ(q)
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setQuotaUser(UUID.randomUUID().toString())
                    .setSupportsTeamDrives(true)
                    .setIncludeItemsFromAllDrives(true)
                    .execute();

            return result.getFiles().stream()
                    .map(File::getId)
                    .findFirst()
                    .orElse(null);
    }
    
    public void deleteFile(String id) throws IOException {
        File toTrash = new File();
        toTrash.setTrashed(true);

        drive.files().update(id, toTrash)
                .setQuotaUser(UUID.randomUUID().toString())
                .setSupportsTeamDrives(true)
                .execute();
    }
}
