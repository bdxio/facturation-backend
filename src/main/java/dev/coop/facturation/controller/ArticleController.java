package dev.coop.facturation.controller;

import dev.coop.facturation.FacturationException;
import dev.coop.facturation.model.Article;
import dev.coop.facturation.model.SocieteCodeKey;
import dev.coop.facturation.persistence.ArticleRepository;
import dev.coop.facturation.persistence.UtilisateurRepository;
import dev.coop.facturation.security.ConnectedUser;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("articles")
public class ArticleController {
    
    @Autowired
    private ArticleRepository articleRepository;
   
    @RequestMapping(value = "/{articleId}", method = RequestMethod.GET)
    public Article getArticle(@AuthenticationPrincipal ConnectedUser user, @PathVariable String articleId) {
        return articleRepository.findOne(SocieteCodeKey.create(user.getUtilisateur().getSociete(), articleId));
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public List<Article> getArticles(@AuthenticationPrincipal ConnectedUser user) {
        return articleRepository.findBySociete(user.getUtilisateur().getSociete());
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public  @ResponseBody Article saveArticle(@AuthenticationPrincipal ConnectedUser user, @RequestBody Article article) {
        if (!article.getSociete().getNom().equals(user.getUtilisateur().getSociete().getNom())) {
            throw new FacturationException("Societe de l'article ne correspond pas à celle de l'user.getUtilisateur()!");
        }
        article.setSociete(user.getUtilisateur().getSociete());
        return articleRepository.save(article);
    }
}
