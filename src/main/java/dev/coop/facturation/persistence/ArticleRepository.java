package dev.coop.facturation.persistence;

import dev.coop.facturation.model.Article;
import org.springframework.stereotype.Component;

@Component
public class ArticleRepository extends InMemoryRepository<Article> {


    @Override
    protected String getEntityName() {
        return "Article";
    }
}
