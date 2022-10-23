package handler

import (
	"errors"
	"io"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"

	"github.com/bdxio/facturation-backend/invoice"
	"github.com/bdxio/facturation-backend/repository/sheet"
)

func TestHTTP_GenerateInvoices(t *testing.T) {
	t.Parallel()

	handler := NewHTTP(
		http.DefaultClient,
		func(sheetID string) (sheet.Reader, error) {
			return fakeReader{}, nil
		},
		func(_ string) (invoice.Storage, error) {
			return fakeStorage{}, nil
		},
	)
	req := httptest.NewRequest(http.MethodGet, "/generateInvoices/sheetID/folderID", nil)
	rec := httptest.NewRecorder()

	handler.GenerateInvoices(rec, req)

	assert.Equal(t, http.StatusOK, rec.Code)
	data, err := io.ReadAll(rec.Body)
	require.NoError(t, err)
	assert.Empty(t, data)
}

//nolint:paralleltest // an HTTP server is started before tests to serve logo, running in parallel won't work.
func TestHTTP_GenerateInvoices_Errors(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
	}))
	t.Cleanup(srv.Close)

	tests := []struct {
		name            string
		url             string
		readerSupplier  ReaderSupplier
		storageSupplier StorageSupplier
		wantCode        int
		wantBody        string
	}{
		{
			name: "missing folderID in URL",
			url:  "/generateInvoices/sheetID",
			readerSupplier: func(sheetID string) (sheet.Reader, error) {
				return fakeReader{}, nil
			},
			storageSupplier: func(folderID string) (invoice.Storage, error) {
				return fakeStorage{}, nil
			},
			wantCode: http.StatusBadRequest,
			wantBody: "Missing sheetID or folderID in path\n",
		},
		{
			name: "readerSupplier error",
			url:  "/generateInvoices/sheetID/folderID",
			readerSupplier: func(sheetID string) (sheet.Reader, error) {
				return nil, errors.New("reader supplier error")
			},
			storageSupplier: func(folderID string) (invoice.Storage, error) {
				return fakeStorage{}, nil
			},
			wantCode: http.StatusInternalServerError,
			wantBody: "Could not create Google sheet reader: reader supplier error\n",
		},
		{
			name: "repository creation error",
			url:  "/generateInvoices/sheetID/folderID",
			readerSupplier: func(sheetID string) (sheet.Reader, error) {
				return fakeReader{items: [][]string{{"ID", "COMPANY", "DESC", "AMOUNT"}}}, nil
			},
			storageSupplier: func(folderID string) (invoice.Storage, error) {
				return fakeStorage{}, nil
			},
			wantCode: http.StatusInternalServerError,
			wantBody: "Could not create repository: could not convert amount for item with ID ID: strconv.Atoi: parsing \"AMOUNT\": invalid syntax\n",
		},
		{
			name: "storageSupplier error",
			url:  "/generateInvoices/sheetID/folderID",
			readerSupplier: func(sheetID string) (sheet.Reader, error) {
				return fakeReader{}, nil
			},
			storageSupplier: func(folderID string) (invoice.Storage, error) {
				return nil, errors.New("storage supplier error")
			},
			wantCode: http.StatusInternalServerError,
			wantBody: "Could not create storage: storage supplier error\n",
		},
		{
			name: "invoice generation error",
			url:  "/generateInvoices/sheetID/folderID",
			readerSupplier: func(sheetID string) (sheet.Reader, error) {
				return fakeReader{
					companies: [][]string{{
						"nom", "Company1", "description", "formeJuridique", "rue1", "rue2", "rue3", "codePostal", "ville", "pays",
						"tel", "fax", "web", "mail", "siret", "naf", "numTvaIntracom", "capital", "iban", "bic", srv.URL,
						"delaiPaiement",
					}},
					customers: [][]string{{
						"Customer1", "Societe_REF", "nom", "nomCourt", "rue1", "rue2", "rue3", "codePostal", "ville", "pays",
						"numTvaIntracom",
					}},
					items: [][]string{
						{"Item1", "Societe_REF", "desc item1", "80", "tva", "unite"},
					},
					invoices: [][]string{
						{"Invoice1", "Company1", "Item1", "Customer1", "02/01/2006", "1", "desc item1", "", "Commentaire"},
					},
				}, nil
			},
			storageSupplier: func(folderID string) (invoice.Storage, error) {
				return fakeStorage{err: errors.New("storage error")}, nil
			},
			wantCode: http.StatusInternalServerError,
			wantBody: "Could not generate invoices: storage error\n",
		},
	}
	for _, tt := range tests {
		tt := tt
		t.Run(tt.name, func(t *testing.T) {
			req := httptest.NewRequest(http.MethodGet, tt.url, nil)
			rec := httptest.NewRecorder()
			handler := NewHTTP(http.DefaultClient, tt.readerSupplier, tt.storageSupplier)

			handler.GenerateInvoices(rec, req)

			data, err := io.ReadAll(rec.Body)
			require.NoError(t, err)

			assert.Equal(t, tt.wantCode, rec.Code)
			assert.Equal(t, tt.wantBody, string(data))
		})
	}
}

type fakeReader struct {
	companies [][]string
	customers [][]string
	items     [][]string
	invoices  [][]string
}

func (f fakeReader) GetCompanies() [][]string { return f.companies }
func (f fakeReader) GetCustomers() [][]string { return f.customers }
func (f fakeReader) GetItems() [][]string     { return f.items }
func (f fakeReader) GetInvoices() [][]string  { return f.invoices }
func (f fakeReader) GetQuotes() [][]string    { return nil }

type fakeStorage struct{ err error }

func (s fakeStorage) Exists(filename string) (bool, error)              { return false, s.err }
func (s fakeStorage) Save(filename, mimeType string, data []byte) error { return s.err }
