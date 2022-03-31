package dev.coop.facturation.persistence;

import dev.coop.facturation.BdxIoInitializer;
import dev.coop.facturation.Facturation;
import dev.coop.facturation.model.Societe;
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
class SocieteRepositoryTest {

    public static final String BDXIO = "BDXIO";

    @Autowired
    private BdxIoInitializer initializer;

    @Autowired
    private SocieteRepository repository;


    @BeforeEach
    public void init() throws Exception {
        initializer.doInit();
    }

    @Test
    void findByIdOrThrow() {
        Societe byId = repository.findByIdOrThrow(BDXIO);
        Assertions.assertEquals(BDXIO, byId.getNomCourt());

        try{
            byId = repository.findByIdOrThrow("WOOT");
            Assertions.assertNull(byId);
        }catch (IllegalStateException e){
            Assertions.assertFalse(e.getMessage().isEmpty());
        }
    }

    @Test
    void testFindAndSaveAndDeleteAll() {

        Optional<Societe> byId = repository.findById(BDXIO);
        Assertions.assertFalse(byId.isEmpty());
        Assertions.assertEquals(BDXIO, byId.get().getNomCourt());

        repository.deleteAll();
        byId = repository.findById(BDXIO);
        Assertions.assertTrue(byId.isEmpty());
    }
}