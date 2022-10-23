package sheet

import (
	"fmt"
	"strconv"
	"time"

	"github.com/bdxio/facturation-backend/domain"
)

const dateParseLayout = "02/01/2006"

type Reader interface {
	GetCompanies() [][]string
	GetCustomers() [][]string
	GetItems() [][]string
	GetInvoices() [][]string
	GetQuotes() [][]string
}

type Downloader interface {
	Download(url string) (string, error)
}

type Repository struct {
	invoices []*domain.Invoice
	quotes   []*domain.Quote
}

func NewRepository(reader Reader, downloader Downloader) (Repository, error) {
	companies, err := parseCompanies(reader.GetCompanies(), downloader)
	if err != nil {
		return Repository{}, err
	}
	customers := parseCustomers(reader.GetCustomers())
	items, err := parseItems(reader.GetItems())
	if err != nil {
		return Repository{}, err
	}
	invoices, err := parseInvoices(reader.GetInvoices(), companies, customers, items)
	if err != nil {
		return Repository{}, err
	}
	quotes, err := parseQuotes(reader.GetQuotes(), companies, customers, items)
	if err != nil {
		return Repository{}, err
	}

	return Repository{
		invoices: invoices,
		quotes:   quotes,
	}, nil
}

func parseCompanies(rows [][]string, downloader Downloader) (map[string]domain.Company, error) {
	companies := make(map[string]domain.Company, len(rows))
	for _, row := range rows {
		// shortName is used as reference for other entities
		shortName := row[1]
		logoURL := row[20]
		if logoURL == "" {
			return nil, fmt.Errorf("logo for company %s not defined", shortName)
		}
		logo, err := downloader.Download(logoURL)
		if err != nil {
			return nil, fmt.Errorf("could not download logo for company %s: %w", shortName, err)
		}
		company := domain.Company{
			Name:      row[0],
			ShortName: shortName,
			Desc:      row[2],
			Address: domain.Address{
				Street1: row[4],
				Street2: row[5],
				Street3: row[6],
				ZipCode: row[7],
				City:    row[8],
			},
			URL:   row[12],
			Email: row[13],
			SIRET: row[14],
			IBAN:  row[18],
			BIC:   row[19],
			Logo:  logo,
		}
		companies[shortName] = company
	}
	return companies, nil
}

func parseCustomers(rows [][]string) map[string]domain.Customer {
	customers := make(map[string]domain.Customer, len(rows))
	for _, row := range rows {
		id := row[0]
		customer := domain.Customer{
			ID:        id,
			Name:      row[2],
			ShortName: row[3],
			Address: domain.Address{
				Street1: row[4],
				Street2: row[5],
				Street3: row[6],
				ZipCode: row[7],
				City:    row[8],
			},
		}
		customers[id] = customer
	}
	return customers
}

func parseItems(rows [][]string) (map[string]domain.Item, error) {
	items := make(map[string]domain.Item, len(rows))
	for _, row := range rows {
		id := row[0]
		amount, err := strconv.Atoi(row[3])
		if err != nil {
			return nil, fmt.Errorf("could not convert amount for item with ID %s: %w", id, err)
		}
		item := domain.Item{
			ID:     id,
			Desc:   row[2],
			Amount: amount,
		}
		items[id] = item
	}
	return items, nil
}

func parseInvoices(rows [][]string, companies map[string]domain.Company, customers map[string]domain.Customer, items map[string]domain.Item) ([]*domain.Invoice, error) {
	invoices := make([]*domain.Invoice, 0, len(rows))
	invoice := &domain.Invoice{}
	for _, row := range rows {
		id := row[0]

		itemID := row[2]
		item, ok := items[itemID]
		if !ok {
			return nil, fmt.Errorf("no item with ID %s found for invoice line with id %s", itemID, id)
		}

		qty, err := strconv.Atoi(row[5])
		if err != nil {
			return nil, fmt.Errorf("could not convert qty for invoice line with ID %s: %w", id, err)
		}

		deposit := 0
		if row[7] != "" {
			deposit, err = strconv.Atoi(row[7])
			if err != nil {
				return nil, fmt.Errorf("could not convert deposit for invoice line with ID %s: %w", id, err)
			}
		}

		line := domain.InvoiceLine{
			Item:    item,
			Qty:     qty,
			Desc:    row[6],
			Deposit: domain.Percent(deposit),
		}

		if id == invoice.ID {
			invoice.Lines = append(invoice.Lines, line)
			continue
		}

		companyID := row[1]
		company, ok := companies[companyID]
		if !ok {
			return nil, fmt.Errorf("no company with ID %s found for invoice line with id %s", companyID, id)
		}

		customerID := row[3]
		customer, ok := customers[customerID]
		if !ok {
			return nil, fmt.Errorf("no customer with ID %s found for invoice line with id %s", customerID, id)
		}

		date, err := time.Parse(dateParseLayout, row[4])
		if err != nil {
			return nil, fmt.Errorf("could not convert date for invoice line with ID %s: %w", id, err)
		}

		lines := make([]domain.InvoiceLine, 0)
		lines = append(lines, line)
		invoice = &domain.Invoice{ID: id, Company: company, Customer: customer, Date: date, Lines: lines}
		invoices = append(invoices, invoice)
	}
	return invoices, nil
}

func parseQuotes(rows [][]string, companies map[string]domain.Company, customers map[string]domain.Customer, items map[string]domain.Item) ([]*domain.Quote, error) {
	quotes := make([]*domain.Quote, 0, len(rows))
	quote := &domain.Quote{}
	for _, row := range rows {
		id := row[0]

		itemID := row[2]
		item, ok := items[itemID]
		if !ok {
			return nil, fmt.Errorf("no item with ID %s found for quote line with id %s", itemID, id)
		}

		qty, err := strconv.Atoi(row[5])
		if err != nil {
			return nil, fmt.Errorf("could not convert qty for quote line with ID %s: %w", id, err)
		}

		line := domain.QuoteLine{
			Item: item,
			Qty:  qty,
			Desc: row[6],
		}

		if id == quote.ID {
			quote.Lines = append(quote.Lines, line)
			continue
		}

		companyID := row[1]
		company, ok := companies[companyID]
		if !ok {
			return nil, fmt.Errorf("no company with ID %s found for quote line with id %s", companyID, id)
		}

		customerID := row[3]
		customer, ok := customers[customerID]
		if !ok {
			return nil, fmt.Errorf("no customer with ID %s found for quote line with id %s", customerID, id)
		}

		date, err := time.Parse(dateParseLayout, row[4])
		if err != nil {
			return nil, fmt.Errorf("could not convert date for quote line with ID %s: %w", id, err)
		}

		lines := make([]domain.QuoteLine, 0)
		lines = append(lines, line)
		quote = &domain.Quote{
			ID:       id,
			Company:  company,
			Customer: customer,
			Date:     date,
			Lines:    lines,
		}

		quotes = append(quotes, quote)
	}
	return quotes, nil
}

func (r Repository) GetInvoices() ([]domain.Invoice, error) {
	invoices := make([]domain.Invoice, 0, len(r.invoices))
	for _, invoice := range r.invoices {
		invoices = append(invoices, *invoice)
	}
	return invoices, nil
}

func (r Repository) GetQuotes() ([]domain.Quote, error) {
	quotes := make([]domain.Quote, 0, len(r.quotes))
	for _, quote := range r.quotes {
		quotes = append(quotes, *quote)
	}
	return quotes, nil
}
