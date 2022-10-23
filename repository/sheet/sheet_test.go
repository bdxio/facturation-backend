package sheet

import (
	"errors"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"

	"github.com/bdxio/facturation-backend/domain"
)

var (
	company = domain.Company{
		Name:      "nom",
		ShortName: "Company1",
		Desc:      "description",
		Address: domain.Address{
			Street1: "rue1",
			Street2: "rue2",
			Street3: "rue3",
			ZipCode: "codePostal",
			City:    "ville",
		},
		URL:   "web",
		Email: "mail",
		SIRET: "siret",
		IBAN:  "iban",
		BIC:   "bic",
		Logo:  "logo",
	}

	customer = domain.Customer{
		ID:        "Customer1",
		Name:      "nom",
		ShortName: "nomCourt",
		Address: domain.Address{
			Street1: "rue1",
			Street2: "rue2",
			Street3: "rue3",
			ZipCode: "codePostal",
			City:    "ville",
		},
	}

	item1 = domain.Item{ID: "Item1", Desc: "desc item1", Amount: 80}
	item2 = domain.Item{ID: "Item2", Desc: "desc item2", Amount: 40}
)

func TestNewRepository(t *testing.T) {
	t.Parallel()

	reader := newFakeReader(
		[][]string{
			{"Invoice1", "Company1", "Item1", "Customer1", "02/01/2006", "1", "desc item1", "", "Commentaire"},
			{"Invoice1", "Company1", "Item2", "Customer1", "02/01/2006", "5", "desc item2", "50", "Commentaire"},
			{"Invoice2", "Company1", "Item2", "Customer1", "03/01/2006", "10", "desc item2", "", "Commentaire"},
		},
		[][]string{
			{"Quote1", "Company1", "Item1", "Customer1", "04/01/2006", "5", "desc item1", "Commentaire"},
			{"Quote2", "Company1", "Item1", "Customer1", "05/01/2006", "6", "desc item1", "Commentaire"},
			{"Quote2", "Company1", "Item2", "Customer1", "05/01/2006", "7", "desc item2", "Commentaire"},
		},
	)

	repository, err := NewRepository(reader, stubDownloader{})
	require.NoError(t, err)

	invoices, err := repository.GetInvoices()
	require.NoError(t, err)
	assert.Len(t, invoices, 2)
	assert.ElementsMatch(t, invoices, []domain.Invoice{
		{
			ID:       "Invoice1",
			Company:  company,
			Customer: customer,
			Date:     time.Date(2006, 1, 2, 0, 0, 0, 0, time.UTC),
			Lines: []domain.InvoiceLine{
				{Item: item1, Qty: 1, Desc: "desc item1", Deposit: 0},
				{Item: item2, Qty: 5, Desc: "desc item2", Deposit: 50},
			},
		},
		{
			ID:       "Invoice2",
			Company:  company,
			Customer: customer,
			Date:     time.Date(2006, 1, 3, 0, 0, 0, 0, time.UTC),
			Lines: []domain.InvoiceLine{
				{Item: item2, Qty: 10, Desc: "desc item2", Deposit: 0},
			},
		},
	})

	quotes, err := repository.GetQuotes()
	require.NoError(t, err)
	assert.Len(t, quotes, 2)
	assert.ElementsMatch(t, quotes, []domain.Quote{
		{
			ID:       "Quote1",
			Company:  company,
			Customer: customer,
			Date:     time.Date(2006, 1, 4, 0, 0, 0, 0, time.UTC),
			Lines: []domain.QuoteLine{
				{Item: item1, Qty: 5, Desc: "desc item1"},
			},
		},
		{
			ID:       "Quote2",
			Company:  company,
			Customer: customer,
			Date:     time.Date(2006, 1, 5, 0, 0, 0, 0, time.UTC),
			Lines: []domain.QuoteLine{
				{Item: item1, Qty: 6, Desc: "desc item1"},
				{Item: item2, Qty: 7, Desc: "desc item2"},
			},
		},
	})
}

func TestNewRepository_Errors(t *testing.T) {
	t.Parallel()

	tests := []struct {
		name       string
		reader     fakeReader
		downloader Downloader
		wantErr    string
	}{
		{
			name: "no logo",
			reader: fakeReader{
				companies: [][]string{{
					"nom", "Company1", "pas de logo", "formeJuridique", "rue1", "rue2", "rue3", "codePostal", "ville", "pays",
					"tel", "fax", "web", "mail", "siret", "naf", "numTvaIntracom", "capital", "iban", "bic", "",
					"delaiPaiement",
				}},
			},
			downloader: stubDownloader{},
			wantErr:    "logo for company Company1 not defined",
		},
		{
			name:       "logo download failure",
			reader:     newFakeReader(nil, nil),
			downloader: failingDownloader{},
			wantErr:    "could not download logo for company Company1: download failed",
		},
		{
			name: "invalid item amount",
			reader: fakeReader{
				items: [][]string{{
					"Item1", "Societe_REF", "desc item1", "invalid amount", "tva", "unite",
				}},
			},
			downloader: stubDownloader{},
			wantErr:    `could not convert amount for item with ID Item1: strconv.Atoi: parsing "invalid amount": invalid syntax`,
		},
		{
			name: "invalid invoice line qty",
			reader: newFakeReader(
				[][]string{{
					"Invoice1", "Company1", "Item1", "Customer1", "02/01/2006", "invalid qty", "desc item1", "0", "Commentaire",
				}},
				nil,
			),
			downloader: stubDownloader{},
			wantErr:    `could not convert qty for invoice line with ID Invoice1: strconv.Atoi: parsing "invalid qty": invalid syntax`,
		},
		{
			name: "invalid invoice line deposit",
			reader: newFakeReader(
				[][]string{{
					"Invoice1", "Company1", "Item1", "Customer1", "02/01/2006", "1", "desc item1", "invalid deposit", "Commentaire",
				}},
				nil,
			),
			downloader: stubDownloader{},
			wantErr:    `could not convert deposit for invoice line with ID Invoice1: strconv.Atoi: parsing "invalid deposit": invalid syntax`,
		},
		{
			name: "unknown customer in invoice line",
			reader: newFakeReader(
				[][]string{{
					"Invoice1", "UnknownCompany", "Item1", "Customer1", "02/01/2006", "1", "desc item1", "", "Commentaire",
				}},
				nil,
			),
			downloader: stubDownloader{},
			wantErr:    "no company with ID UnknownCompany found for invoice line with id Invoice1",
		},
		{
			name: "unknown item in invoice line",
			reader: newFakeReader(
				[][]string{{
					"Invoice1", "Company1", "UnknownItem", "Customer1", "02/01/2006", "1", "desc item1", "", "Commentaire",
				}},
				nil,
			),
			downloader: stubDownloader{},
			wantErr:    "no item with ID UnknownItem found for invoice line with id Invoice1",
		},
		{
			name: "unknown customer in invoice line",
			reader: newFakeReader(
				[][]string{{
					"Invoice1", "Company1", "Item1", "UnknownCustomer", "02/01/2006", "1", "desc item1", "", "Commentaire",
				}},
				nil,
			),
			downloader: stubDownloader{},
			wantErr:    "no customer with ID UnknownCustomer found for invoice line with id Invoice1",
		},
		{
			name: "invalid invoice date",
			reader: newFakeReader(
				[][]string{{
					"Invoice1", "Company1", "Item1", "Customer1", "invalid date", "1", "desc item1", "", "Commentaire",
				}},
				nil,
			),
			downloader: stubDownloader{},
			wantErr:    `could not convert date for invoice line with ID Invoice1: parsing time "invalid date" as "02/01/2006": cannot parse "invalid date" as "02"`,
		},
		{
			name: "unknown item in quote line",
			reader: newFakeReader(
				nil,
				[][]string{{
					"Quote1", "Company1", "UnknownItem", "Customer1", "04/01/2006", "5", "desc item1", "Commentaire",
				}},
			),
			downloader: stubDownloader{},
			wantErr:    "no item with ID UnknownItem found for quote line with id Quote1",
		},
		{
			name: "invalid qty in quote line",
			reader: newFakeReader(
				nil,
				[][]string{{
					"Quote1", "Company1", "Item1", "Customer1", "04/01/2006", "invalid qty", "desc item1", "Commentaire",
				}},
			),
			downloader: stubDownloader{},
			wantErr:    `could not convert qty for quote line with ID Quote1: strconv.Atoi: parsing "invalid qty": invalid syntax`,
		},
		{
			name: "unknown company in quote line",
			reader: newFakeReader(
				nil,
				[][]string{{
					"Quote1", "UnknownCompany", "Item1", "Customer1", "04/01/2006", "5", "desc item1", "Commentaire",
				}},
			),
			downloader: stubDownloader{},
			wantErr:    "no company with ID UnknownCompany found for quote line with id Quote1",
		},
		{
			name: "unknown customer in quote line",
			reader: newFakeReader(
				nil,
				[][]string{{
					"Quote1", "Company1", "Item1", "UnknownCustomer", "04/01/2006", "5", "desc item1", "Commentaire",
				}},
			),
			downloader: stubDownloader{},
			wantErr:    "no customer with ID UnknownCustomer found for quote line with id Quote1",
		},
		{
			name: "invalid date in quote line",
			reader: newFakeReader(
				nil,
				[][]string{{
					"Quote1", "Company1", "Item1", "Customer1", "invalid date", "5", "desc item1", "Commentaire",
				}},
			),
			downloader: stubDownloader{},
			wantErr:    `could not convert date for quote line with ID Quote1: parsing time "invalid date" as "02/01/2006": cannot parse "invalid date" as "02"`,
		},
	}

	for _, tt := range tests {
		tt := tt
		t.Run(tt.name, func(t *testing.T) {
			t.Parallel()

			_, err := NewRepository(tt.reader, tt.downloader)

			assert.EqualError(t, err, tt.wantErr)
		})
	}
}

type fakeReader struct {
	companies [][]string
	customers [][]string
	items     [][]string
	invoices  [][]string
	quotes    [][]string
}

func newFakeReader(invoices, quotes [][]string) fakeReader {
	return fakeReader{
		companies: [][]string{{
			"nom", "Company1", "description", "formeJuridique", "rue1", "rue2", "rue3", "codePostal", "ville", "pays",
			"tel", "fax", "web", "mail", "siret", "naf", "numTvaIntracom", "capital", "iban", "bic", "logo",
			"delaiPaiement",
		}},
		customers: [][]string{{
			"Customer1", "Societe_REF", "nom", "nomCourt", "rue1", "rue2", "rue3", "codePostal", "ville", "pays",
			"numTvaIntracom",
		}},
		items: [][]string{
			{"Item1", "Societe_REF", "desc item1", "80", "tva", "unite"},
			{"Item2", "Societe_REF", "desc item2", "40", "tva", "unite"},
		},
		invoices: invoices,
		quotes:   quotes,
	}
}

func (r fakeReader) GetCompanies() [][]string { return r.companies }
func (r fakeReader) GetCustomers() [][]string { return r.customers }
func (r fakeReader) GetItems() [][]string     { return r.items }
func (r fakeReader) GetInvoices() [][]string  { return r.invoices }
func (r fakeReader) GetQuotes() [][]string    { return r.quotes }

type stubDownloader struct{}

func (d stubDownloader) Download(url string) (string, error) { return url, nil }

type failingDownloader struct{}

func (d failingDownloader) Download(url string) (string, error) {
	return "", errors.New("download failed")
}
