package invoice

import (
	"github.com/bdxio/facturation-backend/domain"
)

type Service struct {
	repository Repository
	generator  Generator
	storage    Storage
}

func NewService(r Repository, g Generator, s Storage) Service {
	return Service{repository: r, generator: g, storage: s}
}

type Repository interface {
	GetInvoices() ([]domain.Invoice, error)
	GetQuotes() ([]domain.Quote, error)
}

type Generator interface {
	GenerateInvoice(invoice domain.Invoice) ([]byte, error)
	GenerateQuote(quote domain.Quote) ([]byte, error)
	Filename(baseFilename string) string
	MimeType() string
}

type Storage interface {
	Exists(filename string) (bool, error)
	Save(filename, mimeType string, data []byte) error
}

func (s Service) Generate() error {
	if err := s.generateInvoices(); err != nil {
		return err
	}
	return s.generateQuotes()
}

func (s Service) generateInvoices() error {
	invoices, err := s.repository.GetInvoices()
	if err != nil {
		return err
	}

	for _, invoice := range invoices {
		filename := s.generator.Filename(invoice.Name())
		ok, err := s.storage.Exists(filename)
		if err != nil {
			return err
		}
		if ok {
			continue
		}
		data, err := s.generator.GenerateInvoice(invoice)
		if err != nil {
			return err
		}
		if err := s.storage.Save(filename, s.generator.MimeType(), data); err != nil {
			return err
		}
	}

	return nil
}

func (s Service) generateQuotes() error {
	quotes, err := s.repository.GetQuotes()
	if err != nil {
		return err
	}

	for _, quote := range quotes {
		filename := s.generator.Filename(quote.Name())
		ok, err := s.storage.Exists(filename)
		if err != nil {
			return err
		}
		if ok {
			continue
		}
		data, err := s.generator.GenerateQuote(quote)
		if err != nil {
			return err
		}
		if err := s.storage.Save(filename, s.generator.MimeType(), data); err != nil {
			return err
		}
	}

	return nil
}
