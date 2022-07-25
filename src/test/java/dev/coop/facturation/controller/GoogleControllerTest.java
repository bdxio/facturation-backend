package dev.coop.facturation.controller;

import dev.coop.facturation.Facturation;
import dev.coop.facturation.google.GoogleDrive;
import dev.coop.facturation.model.Facture;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Facturation.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GoogleControllerTest {

    private static final String WORKSHEET_ID = "1nguYlIfJvLgnTm9LCKXvd4zo83k2lqwwM9L7UulViSE";
    private static final String GOOGLE_DRIVE_FOLDERID = "1gFqOi6EBjmHSjZk3L633wkpzMszTuGXO";
    private static final String BDXIO = "BDXIO";
    @Autowired
    private GoogleController googleController;

    @Mock
    private GoogleDrive driveMock;

    @BeforeEach
    public void beforeEach() throws IOException {
        //prevent all google drive write!
        Mockito.doNothing().when(driveMock).upload(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        googleController.setGoogleDrive(driveMock);
    }

    @Test
    void getFacturesBySociety() {
        googleController.importSheet(WORKSHEET_ID);
        List<Facture> facturesBySociety = googleController.getFacturesBySociety(BDXIO);
        Approvals.verifyAll("facturesBySociety", facturesBySociety);
        for(Facture facture: facturesBySociety){
            Assertions.assertNotNull(facture.getCode());
        }
    }

    @Test
    void createFileToUpload() {
        googleController.importSheet(WORKSHEET_ID);
        List<Facture> facturesBySociety = googleController.getFacturesBySociety(BDXIO);
        Map<String, byte[]> fileToUpload = googleController.createFileToUpload(GOOGLE_DRIVE_FOLDERID, facturesBySociety);
        Approvals.verifyAll("fileToUpload", fileToUpload.keySet());
    }

}