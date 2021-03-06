# facturation-backend

> Application used to generate invoices from a Google spreadsheet.

## Configuration

The application requires a service account to be able to access Google Drive.  
Once created you need to share the invoices spreadsheet and the folder for the generated invoices with the service account user.  
You'll also need to create an environment variable called `GOOGLE_ACCOUNT` which contains the JSON file generated for the service account, encoded in Base64.

See https://developers.google.com/identity/protocols/oauth2/service-account for more information on service account.

To create the variable in Heroku you can use this command :
```bash
heroku config:set --app=ancient-reaches-59814 GOOGLE_ACCOUNT=$(cat service-account.json | base64 -w 0)
```

## Heroku

The application is currently deployed on Heroku.

### Deployment

To deploy it you just have to follow these steps :

1. install the `heroku` cli
2. ask for Heroku access
3. `heroku login` to login
4. `heroku git:remote --app=<HEROKU APP>` to add Heroku as Git remote
5. `git push heroku` to deploy your current master branch or `git push heroku <BRANCH>:master` to deploy your branch named <BRANCH> to Heroku (_only Heroku master branch is deployed_).

_Note :_ it is also possible to deploy from GitHub but you'll first need to push the branch to GitHub.

### Provisioning

To provision the application you need to create a new Heroku application and enable the mLab MongoDB add-on.

## Local

To use locally the application you just have to start a local MongoDB instance using `docker-compose`:
```
docker-compose up
```

Then configure the Google service account to use in [application.yaml](./src/main/resources/application.yaml) 
and start the application.
