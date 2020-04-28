package dev.coop.facturation.persistence;

import dev.coop.facturation.DevcoopInitializer;
import dev.coop.facturation.Facturation;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Facturation.class)
public class ArticleRepositoryTest {

    @Autowired
    private DevcoopInitializer initializer;
    
    @Autowired
    private ArticleRepository articleRepository;    
    
    
    @Before
    public void init() throws IOException {
        initializer.doInit();
    }
    
     
    @Test
    public void deleteArticle() {
        articleRepository.deleteAll();
    }
    
    
    
}
