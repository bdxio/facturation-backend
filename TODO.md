=> https://developers.google.com/sheets/api/guides/values#writing_multiple_ranges

- Test e2e couvrant l'import de la base et la génération des factures :
  - [ ] brancher un Google Sheet fournissant des données correctes
  - [ ] brancher un MongoDB (TestContainers ?)
  - [ ] brancher un dossier Drive pour générer les PDFs

Une fois ce test e2e fait et exécuté certains points seront à améliorer :
  - [ ] générer au début du test la sheet avec les données
  - [ ] créer un dossier Drive
  - [ ] comparer les noms de fichers, la taille et les hash (SHA1) des PDFs générés à ceux attendus (2 factures et 1 devis)
  - [ ] supprimer le dossier Drive (et les fichiers)
  - [ ] supprimer la sheet Google
  - [ ] documenter la connexion au compte Google (fichier .env contenant le JSON credentials ?)

Lorsque le test e2e pourra être reproduit facilement il faudra faire sauter la partie MongoDB puisque le but est de le remplacer par du InMemory :
  - [ ] désactiver MongoDB pour le test e2e => le test échoue
  - [ ] remonter la couche des dépendances pour remplacer MongoDB par du InMemory en couvrant chaque composant par un mix tests IT et tests unitaires
  - [ ] rise & repeat :-)

Les tests d'intégration nécessiteront sans doute la mise en place de "mocks" pour Google Sheets et Google Drive => fichier JSON et répertoire local :-)

Une fois MongoDB supprimée les dépendances au driver MongoDB et Spring Data MongoDB pourront être supprimées.

Il pourra ensuite être temps d'améliorer quelques sujets :
  - [ ] supprimer les controllers "CRUD"
  - [ ] faire un unique endpoint pour l'import et la génération des factures. Ce endpoint pourra récupérer les données "localement" dans la méthode
  - [ ] supprimer la dépendance à Guava (si possible)
  - [ ] mettre à jour Spring Boot
  - [ ] passer à Java 17
  - [ ] ~~passer à Kotlin~~ won't


- [ ] Demander à Jérôme si il a accès au compte bdxio@bdxio.fr (la connexion requiert le numéro de téléphone)
- [ ] Demander à Nicolas si il est possible de supprimer le projet bdxio-facturation-backend
