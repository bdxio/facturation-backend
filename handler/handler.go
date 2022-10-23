package handler

import (
	"fmt"
	"io"
	"net/http"
	"strings"

	"github.com/bdxio/facturation-backend/invoice"
	"github.com/bdxio/facturation-backend/logo"
	"github.com/bdxio/facturation-backend/pdf"
	"github.com/bdxio/facturation-backend/repository/sheet"
)

type HTTP struct {
	client          *http.Client
	readerSupplier  ReaderSupplier
	storageSupplier StorageSupplier
	downloader      sheet.Downloader
}

func NewHTTP(c *http.Client, r ReaderSupplier, s StorageSupplier) HTTP {
	return HTTP{
		client:          c,
		readerSupplier:  r,
		storageSupplier: s,
		downloader:      logo.NewDownloader(c, io.ReadAll),
	}
}

type ReaderSupplier func(sheetID string) (sheet.Reader, error)

type StorageSupplier func(folderID string) (invoice.Storage, error)

func (h HTTP) GenerateInvoices(w http.ResponseWriter, req *http.Request) {
	path := strings.TrimPrefix(req.RequestURI, "/generateInvoices/")
	params := strings.Split(path, "/")
	if len(params) != 2 {
		http.Error(w, "Missing sheetID or folderID in path", http.StatusBadRequest)
		return
	}

	sheetID := params[0]
	reader, err := h.readerSupplier(sheetID)
	if err != nil {
		http.Error(w, fmt.Sprintf("Could not create Google sheet reader: %v", err), http.StatusInternalServerError)
		return
	}
	repository, err := sheet.NewRepository(reader, h.downloader)
	if err != nil {
		http.Error(w, fmt.Sprintf("Could not create repository: %v", err), http.StatusInternalServerError)
		return
	}

	folderID := params[1]
	storage, err := h.storageSupplier(folderID)
	if err != nil {
		http.Error(w, fmt.Sprintf("Could not create storage: %v", err), http.StatusInternalServerError)
		return
	}

	svc := invoice.NewService(repository, pdf.Generator{}, storage)
	err = svc.Generate()
	if err != nil {
		http.Error(w, fmt.Sprintf("Could not generate invoices: %v", err), http.StatusInternalServerError)
		return
	}
	w.WriteHeader(http.StatusOK)
}
