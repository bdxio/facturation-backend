package dev.coop.facturation.google;

import dev.coop.facturation.Facturation;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author lfo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Facturation.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GoogleDriveTest {
    @Autowired
    private GoogleDrive googleDrive;
    
    
    @Test
    public void testCreate() throws IOException {


        String folderId = "1-Owr1AUhBm9E00NdhiK0z_7A06J6WAmY";
        String tototxt = "toto.txt";
//        googleDrive.upload(folderId, tototxt, "plain/txt", "toto".getBytes()); //remove

        String toto = googleDrive.getFile(tototxt, folderId);
        googleDrive.deleteFile(toto);

       
    }
}
