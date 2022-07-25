# facturation-backend

> Application used to generate invoices from a Google spreadsheet.

## Configuration

The application requires a service account to be able to access Google Drive.  
Once created you need to share the invoices spreadsheet and the folder for the generated invoices with the service account user.  
You'll also need to create an environment variable called `GOOGLE_ACCOUNT` which contains the JSON file generated for the service account, encoded in Base64.

See https://developers.google.com/identity/protocols/oauth2/service-account for more information on service account.

To create a service account :
1. go to [Google Cloud Platform Console](https://console.cloud.google.co)
2. create a new project, called for example "bdxio-facturation-backend"
3. go to APIs & Services to enable Google Drive API and Google Sheets API
4. go to IAM & Admin > Service Accounts to create a service account, called for example "bdxio-facturation-backend" 
5. generate a new key and download the JSON file containing the private key
6. generate a properties file containing the JSON content encoded in Base64 :

    `echo google.account=$(cat <FILE>.json | base64 --wrap=0) >src/test/resources/google.properties`

DO NOT COMMIT THIS FILE !
I REPEAT, DO NOT COMMIT THIS FILE !!

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
5. `git push heroku` to deploy your current main branch or `git push heroku <BRANCH>:main` to deploy your branch named <BRANCH> to Heroku (_only Heroku main branch is deployed_).

_Note :_ it is also possible to deploy from GitHub but you'll first need to push the branch to GitHub.

### Provisioning

To provision the application you need to create a new Heroku application.

## Usage

The application has two HTTP endpoints:

1. one to import the spreadsheet (in memory)
2. one to generate the invoices as PDF

### Spreadsheet import

Hit http://BASE_URL/importInMongo/{worksheetId} to import the spreadsheet in the in-memory database.

`worksheetId` is the id of the spreadsheet to import. It can be found when opened in Google Drive, in the URL.

### Invoices generation

Hit http://BASE_URL/generateInDrive/{worksheetId}/{folderId} to generate the invoices in PDF format.

`worksheetId` is the id of the spreadsheet containing the invoices to generate.
`folderId` is the id of the folder where the generated invoices should be put.

Both ids can be found in Google Drive when opening the spreadsheet or the folder, their id is in the URL.

Only new invoices will be generated.  
To generate again an existing invoice remove the previous one.

## Local

To use locally the application you just have to configure the Google service account to use in [application.yaml](./src/main/resources/application.yaml) 
and start the application.

You can use the service account to use the production spreadsheet and then hit your local application to generate the invoices on Google Drive.  
To retrieve the service account credentials you can use the Heroku cli:
```sh
heroku config:get GOOGLE_ACCOUNT
```

Put the retrieved value as is in the application.yaml configuration file.

