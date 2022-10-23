package main

import (
	"context"
	"encoding/base64"
	"encoding/json"
	"errors"
	"flag"
	"fmt"
	"io"
	"log"
	"net"
	"net/http"
	"os"
	"time"

	"golang.org/x/oauth2/google"
	"golang.org/x/oauth2/jwt"

	"github.com/bdxio/facturation-backend/handler"
	"github.com/bdxio/facturation-backend/invoice"
	"github.com/bdxio/facturation-backend/logo"
	"github.com/bdxio/facturation-backend/pdf"
	"github.com/bdxio/facturation-backend/repository/sheet"
	"github.com/bdxio/facturation-backend/storage/drive"
	"github.com/bdxio/facturation-backend/storage/fs"
)

func main() {
	var cli bool
	var sheetID string
	var dir string
	var folderID string

	flag.BoolVar(&cli, "cli", false, "run as a CLI")
	flag.StringVar(&sheetID, "sheet-id", "", "Google sheet ID")
	flag.StringVar(&dir, "dir", "", "local directory to store generated invoices")
	flag.StringVar(&folderID, "folder-id", "", "Google Drive folder ID to store generated invoices")
	flag.Parse()

	client, err := newGoogleHTTPClient()
	if err != nil {
		log.Fatalf("Could not create HTTP client: %v", err)
	}

	if cli {
		err := runCLI(sheetID, dir, folderID, client)
		if err != nil {
			log.Fatalf("Could not generate invoices: %v", err)
		}
		return
	}

	runHTTP(dir, client)
}

func runCLI(sheetID, dir, folderID string, client *http.Client) error {
	if sheetID == "" {
		fmt.Println("sheet argument should be provided")
		flag.Usage()
		os.Exit(1)
	}

	start := time.Now()
	reader, err := sheet.NewGoogleReader(client, sheetID)
	if err != nil {
		log.Fatalf("Could not create Google sheet reader: %v", err)
	}
	repository, err := sheet.NewRepository(reader, logo.NewDownloader(http.DefaultClient, io.ReadAll))
	if err != nil {
		log.Fatalf("Could not create repository: %v", err)
	}

	var storage invoice.Storage
	if dir == "" {
		storage, err = drive.NewStorage(client, folderID)
		if err != nil {
			return err
		}
	} else {
		storage = fs.NewStorage(dir)
	}

	svc := invoice.NewService(repository, pdf.Generator{}, storage)
	err = svc.Generate()
	if err != nil {
		log.Fatalf("Could not generate invoices: %v", err)
	}
	fmt.Printf("Generated all invoices in %v\n", time.Since(start))

	return nil
}

func runHTTP(dir string, client *http.Client) {
	var storageSupplier handler.StorageSupplier
	if dir == "" {
		storageSupplier = func(folderID string) (invoice.Storage, error) {
			return drive.NewStorage(client, folderID)
		}
	} else {
		storageSupplier = func(_ string) (invoice.Storage, error) {
			return fs.NewStorage(dir), nil
		}
	}

	port := os.Getenv("PORT")
	if port == "" {
		port = "0"
	}
	l, err := net.Listen("tcp", fmt.Sprintf(":%s", port))
	if err != nil {
		log.Fatal(err)
	}
	h := handler.NewHTTP(
		client,
		func(sheetID string) (sheet.Reader, error) {
			return sheet.NewGoogleReader(client, sheetID)
		},
		storageSupplier,
	)
	http.HandleFunc("/generateInvoices/", h.GenerateInvoices)
	log.Printf("Started HTTP server on %s...", l.Addr())
	log.Fatal(http.Serve(l, nil))
}

type GoogleCredentials struct {
	PrivateKeyID string `json:"private_key_id"`
	PrivateKey   string `json:"private_key"`
	ClientEmail  string `json:"client_email"`
}

func newGoogleHTTPClient() (*http.Client, error) {
	googleAccount := os.Getenv("GOOGLE_ACCOUNT")
	if googleAccount == "" {
		return nil, errors.New("GOOGLE_ACCOUNT environment variable should be defined")
	}

	jsonKey, err := base64.StdEncoding.DecodeString(googleAccount)
	if err != nil {
		return nil, fmt.Errorf("could not decode GOOGLE_ACCOUNT from base64: %w", err)
	}

	var creds GoogleCredentials
	if err := json.Unmarshal(jsonKey, &creds); err != nil {
		return nil, fmt.Errorf("could not unmarshal Google credentials : %w", err)
	}

	config := jwt.Config{
		Email:        creds.ClientEmail,
		PrivateKey:   []byte(creds.PrivateKey),
		PrivateKeyID: creds.PrivateKeyID,
		Scopes: []string{
			"https://www.googleapis.com/auth/spreadsheets.readonly",
			"https://www.googleapis.com/auth/drive",
			"https://www.googleapis.com/auth/drive.file",
		},
		TokenURL: google.JWTTokenURL,
	}

	return config.Client(context.Background()), nil
}
