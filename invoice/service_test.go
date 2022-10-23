package invoice

import (
	"errors"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"

	"github.com/bdxio/facturation-backend/domain"
)

var (
	company1 = domain.Company{
		Name:      "My Company",
		ShortName: "Company1",
	}
	company2 = domain.Company{
		Name:      "My Other Company",
		ShortName: "Company2",
	}
	customer1 = domain.Customer{
		ID:        "1",
		Name:      "A Customer",
		ShortName: "Customer1",
	}
	customer2 = domain.Customer{
		ID:        "2",
		Name:      "Another Customer",
		ShortName: "Customer2",
	}
	invoice1 = domain.Invoice{
		ID:       "1",
		Company:  company1,
		Customer: customer1,
	}
	invoice2 = domain.Invoice{
		ID:       "2",
		Company:  company2,
		Customer: customer2,
	}
	quote1 = domain.Quote{
		ID:       "1",
		Company:  company1,
		Customer: customer1,
	}
	quote2 = domain.Quote{
		ID:       "2",
		Company:  company2,
		Customer: customer2,
	}
)

func TestService_GenerateInvoice(t *testing.T) {
	t.Parallel()

	repository := newFakeRepository(
		withInvoices([]domain.Invoice{invoice1, invoice2}),
		withQuotes([]domain.Quote{quote1, quote2}),
	)
	storage := newFakeStorage()
	svc := NewService(repository, fakeGenerator{}, storage)

	err := svc.Generate()
	require.NoError(t, err)

	assert.Equal(t, 4, len(storage.stored))
	assert.Contains(t, storage.stored, invoice1.Name(), invoice2.Name(), quote1.Name(), quote2.Name())
}

func TestService_Generate_Exists(t *testing.T) {
	t.Parallel()

	repository := newFakeRepository(
		withInvoices([]domain.Invoice{invoice1}),
		withQuotes([]domain.Quote{quote1}),
	)
	storage := newFakeStorage(withExists())
	svc := NewService(repository, fakeGenerator{}, storage)

	err := svc.Generate()
	require.NoError(t, err)

	assert.Empty(t, storage.stored)
}

func TestService_Generate_Errors(t *testing.T) {
	t.Parallel()

	tests := []struct {
		name       string
		repository Repository
		generator  Generator
		storage    Storage
		wantErr    string
	}{
		{
			name:       "error when getting invoices",
			repository: newFakeRepository(withInvoicesErr(errors.New("no invoice found"))),
			generator:  fakeGenerator{},
			storage:    newFakeStorage(),
			wantErr:    "no invoice found",
		},
		{
			name:       "storage exists error when checking invoice",
			repository: newFakeRepository(withInvoices([]domain.Invoice{invoice1})),
			generator:  fakeGenerator{},
			storage:    newFakeStorage(withExistsErr(errors.New("invoice exists error"))),
			wantErr:    "invoice exists error",
		},
		{
			name:       "generate invoice error",
			repository: newFakeRepository(withInvoices([]domain.Invoice{invoice1})),
			generator:  fakeGenerator{err: errors.New("invoice generate error")},
			storage:    newFakeStorage(),
			wantErr:    "invoice generate error",
		},
		{
			name:       "storage save error when storing invoice",
			repository: newFakeRepository(withInvoices([]domain.Invoice{invoice1})),
			generator:  fakeGenerator{},
			storage:    newFakeStorage(withSaveErr(errors.New("invoice save error"))),
			wantErr:    "invoice save error",
		},
		{
			name:       "error when getting quotes",
			repository: newFakeRepository(withQuotesErr(errors.New("no quote found"))),
			generator:  fakeGenerator{},
			storage:    newFakeStorage(),
			wantErr:    "no quote found",
		},
		{
			name:       "storage exists error when checking quote",
			repository: newFakeRepository(withQuotes([]domain.Quote{quote1})),
			generator:  fakeGenerator{},
			storage:    newFakeStorage(withExistsErr(errors.New("quote exists error"))),
			wantErr:    "quote exists error",
		},
		{
			name:       "generate quote error",
			repository: newFakeRepository(withQuotes([]domain.Quote{quote1})),
			generator:  fakeGenerator{err: errors.New("quote generate error")},
			storage:    newFakeStorage(),
			wantErr:    "quote generate error",
		},
		{
			name:       "storage save error when storing quote",
			repository: newFakeRepository(withQuotes([]domain.Quote{quote1})),
			generator:  fakeGenerator{},
			storage:    newFakeStorage(withSaveErr(errors.New("quote save error"))),
			wantErr:    "quote save error",
		},
	}
	for _, tt := range tests {
		tt := tt

		t.Run(tt.name, func(t *testing.T) {
			t.Parallel()

			s := NewService(tt.repository, tt.generator, tt.storage)

			err := s.Generate()

			assert.EqualError(t, err, tt.wantErr)
		})
	}
}

type fakeRepository struct {
	invoices    []domain.Invoice
	invoicesErr error
	quotes      []domain.Quote
	quotesErr   error
}

func newFakeRepository(opts ...fakeRepositoryOption) fakeRepository {
	r := fakeRepository{}
	for _, opt := range opts {
		opt(&r)
	}
	return r
}

type fakeRepositoryOption func(r *fakeRepository)

func withInvoices(invoices []domain.Invoice) fakeRepositoryOption {
	return func(r *fakeRepository) {
		r.invoices = invoices
	}
}

func withQuotes(quotes []domain.Quote) fakeRepositoryOption {
	return func(r *fakeRepository) {
		r.quotes = quotes
	}
}

func withInvoicesErr(err error) fakeRepositoryOption {
	return func(r *fakeRepository) {
		r.invoicesErr = err
	}
}

func withQuotesErr(err error) fakeRepositoryOption {
	return func(r *fakeRepository) {
		r.quotesErr = err
	}
}

func (r fakeRepository) GetInvoices() ([]domain.Invoice, error) {
	if r.invoicesErr != nil {
		return nil, r.invoicesErr
	}
	return r.invoices, nil
}

func (r fakeRepository) GetQuotes() ([]domain.Quote, error) {
	if r.quotesErr != nil {
		return nil, r.quotesErr
	}
	return r.quotes, nil
}

type fakeGenerator struct {
	err error
}

func (g fakeGenerator) GenerateInvoice(invoice domain.Invoice) ([]byte, error) {
	if g.err != nil {
		return nil, g.err
	}
	return []byte(invoice.Title()), nil
}

func (g fakeGenerator) GenerateQuote(quote domain.Quote) ([]byte, error) {
	if g.err != nil {
		return nil, g.err
	}
	return []byte(quote.Title()), nil
}

func (g fakeGenerator) MimeType() string { return "fake" }

func (g fakeGenerator) Filename(title string) string { return title }

type fakeStorage struct {
	stored    map[string][]byte
	exists    bool
	existsErr error
	saveErr   error
}

func newFakeStorage(opts ...fakeStorageOption) *fakeStorage {
	s := &fakeStorage{
		stored: make(map[string][]byte),
	}

	for _, opt := range opts {
		opt(s)
	}

	return s
}

type fakeStorageOption func(s *fakeStorage)

func withExists() fakeStorageOption {
	return func(s *fakeStorage) {
		s.exists = true
	}
}

func withExistsErr(err error) fakeStorageOption {
	return func(s *fakeStorage) {
		s.existsErr = err
	}
}

func withSaveErr(err error) fakeStorageOption {
	return func(s *fakeStorage) {
		s.saveErr = err
	}
}

func (s *fakeStorage) Exists(filename string) (bool, error) {
	if s.existsErr != nil {
		return false, s.existsErr
	}
	return s.exists, nil
}

func (s *fakeStorage) Save(filename, mimeType string, data []byte) error {
	if s.saveErr != nil {
		return s.saveErr
	}

	s.stored[filename] = data
	return nil
}
