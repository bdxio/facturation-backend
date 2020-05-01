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
        
        String folderId = "0B9IB8KKHBgPeUVlxRE5FMHRWMUU";
        String tototxt = "toto.txt";
        googleDrive.upload(folderId, tototxt, "plain/txt", "toto".getBytes());
        String toto = googleDrive.getFile(tototxt, folderId);
        googleDrive.deleteFile(toto);

       
    }
}
