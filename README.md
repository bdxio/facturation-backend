# facturation-backend

> Application used to generate invoices and quotes from a Google spreadsheet.

## Provisioning

### Google Drive

The application requires a service account to be able to access Google Drive.  
Once created you need to share the invoices spreadsheet and the folder for the generated invoices with the service account user.

See https://developers.google.com/identity/protocols/oauth2/service-account for more information on service account.

To create a service account:
1. go to [Google Cloud Platform Console](https://console.cloud.google.com)
2. create a new project, called for example "facturation-backend"
3. go to "APIs & Services" to enable Google Drive API and Google Sheets API
4. go to "IAM & Admin > Service Accounts" to create a service account, called for example "facturation-backend"
5. generate a new key and download the JSON file containing the private key

_Note_:
The Google Drive spreadsheet has to be associated to a GCP project (usually the one created previously).  
Go to the settings of the Apps Script to set one in case it is missing.

### Google Cloud Run

The application is currently deployed on Google Cloud Run.

[Install](https://cloud.google.com/sdk/docs/install) the gcloud CLI and 
[initialize](https://cloud.google.com/sdk/docs/initializing) it by running `gcloud init`.

You should be able to select the project created before.

Then you have to enable the Cloud Run API and Cloud Build API (requires an account with billing enabled).  
See this [article](https://cloud.google.com/billing/docs/how-to/verify-billing-enabled?hl=fr) for more information.

As the application needs access to the Google Drive spreadsheet a secret with the content of the service account JSON 
file has to be created:
```shell
cat <SERVICE>.json | base64 --wrap 0 | gcloud secrets create google-account --data-file=- --locations=europe-west1 --replication-policy=user-managed
```

_Note_:
As the application will only be deployed in one region automatic replication is disabled with the 
`--replication-policy=user-managed` option.

Finally, add read-only access to secret manager to the service account created for the application.

## Building

Docker is used to build the application:
```shell
gcloud builds submit --tag gcr.io/facturation-backend/invoice
```

## Deploying

If deploying for the first time use the following command to create the service:
```shell
gcloud run deploy invoice --image=gcr.io/facturation-backend/invoice --update-secrets=GOOGLE_ACCOUNT=google-account:latest --region=europe-west1 --allow-unauthenticated
```

When deploying a new build the following command is sufficient:
```shell
gcloud run deploy invoice --image=gcr.io/facturation-backend/invoice --region=europe-west1
```

## Using

The application has one HTTP endpoint to generate invoices:
```shell
curl https://<SERVICE_URL>/generateInvoices/<SHEET_ID>/<FOLDER_ID>
```
where `SHEET_ID` is the ID of the spreadsheet containing the invoices to generate 
and `FOLDER_ID` the id of the folder where the generated invoices should be saved.

Both IDs can be found in Google Drive when opening the spreadsheet or the folder, their IDs are in the URL.

The service URL can be found using the following command:
```shell
gcloud run services list
```

_Note_:
Only new invoices will be generated.    
To generate again an existing invoice remove the existing one.

## Local

To use locally the application you can just define the GOOGLE_ACCOUNT variable and run the application:
```shell
export GOOGLE_ACCOUNT=$(cat <SERVICE>.json | base64 --wrap 0)
# or for fish lovers
set --export --global GOOGLE_ACCOUNT (cat <SERVICE>.json | base64 --wrap 0)

go run cmd/invoice/main.go
```
