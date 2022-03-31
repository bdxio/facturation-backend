package dev.coop.facturation.persistence;

import dev.coop.facturation.BdxIoInitializer;
import dev.coop.facturation.Facturation;
import dev.coop.facturation.model.Devis;
import dev.coop.facturation.model.SocieteCodeKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Facturation.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DevisRepositoryTest {


    public static final String BDXIO = "BDXIO";

    @Autowired
    private BdxIoInitializer initializer;

    @Autowired
    private DevisRepository repository;


    @BeforeEach
    public void init() throws Exception {
        initializer.doInit();
    }

    @Test
    void findByIdOrThrow() {
        SocieteCodeKey societyCodeKey = SocieteCodeKey.create("BDXIO", 150801);
        Devis byId = repository.findByIdOrThrow(societyCodeKey);
        Assertions.assertEquals(BDXIO, byId.getSociete().getNomCourt());

        SocieteCodeKey woot = SocieteCodeKey.create("woot", 150801);
        try{
            byId = repository.findByIdOrThrow(woot);
            Assertions.assertNull(byId);
        }catch (IllegalStateException e){
            Assertions.assertFalse(e.getMessage().isEmpty());
        }
    }

    @Test
    void testFindAndSaveAndDeleteAll() {
        SocieteCodeKey societyCodeKey = SocieteCodeKey.create("BDXIO", 150801);
        Optional<Devis> byId = repository.findById(societyCodeKey);
        Assertions.assertFalse(byId.isEmpty());
        Assertions.assertEquals(BDXIO, byId.get().getSociete().getNomCourt());

        repository.deleteAll();
        byId = repository.findById(societyCodeKey);
        Assertions.assertTrue(byId.isEmpty());
    }

}