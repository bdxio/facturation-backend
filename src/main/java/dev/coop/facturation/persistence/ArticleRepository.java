package dev.coop.facturation.persistence;

import dev.coop.facturation.model.Article;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.model.SocieteCodeKey;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArticleRepository extends MongoRepository<Article, SocieteCodeKey> {

    public List<Article> findBySociete(Societe societe);

    default Article findByIdOrThrow(SocieteCodeKey id) {
        return this.findById(id)
                .orElseThrow(() -> new IllegalStateException(String.format("L'article %s est inconnue", id)));
    }
}
