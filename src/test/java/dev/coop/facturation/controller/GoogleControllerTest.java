package dev.coop.facturation.controller;

import dev.coop.facturation.Facturation;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Facturation.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GoogleControllerTest {

    @Autowired
    private GoogleController googleController;


    @Test
    void importSheetTest() {
        googleController.importSheet("1S6hdN75AYQqdk9cfC2Ot6dORWZu6u3NfwWzUiPylUdU");
    }

}