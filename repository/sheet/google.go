package sheet

import (
	"context"
	"fmt"
	"net/http"

	"google.golang.org/api/option"
	"google.golang.org/api/sheets/v4"
)

var ranges = []string{
	"Societe!A2:V",
	"Client!A2:K",
	"Article!A2:E",
	"Facture!A2:I",
	"Devis!A2:H",
}

type GoogleReader struct {
	companies [][]string
	customers [][]string
	items     [][]string
	invoices  [][]string
	quotes    [][]string
}

func NewGoogleReader(client *http.Client, sheetID string) (GoogleReader, error) {
	srv, err := sheets.NewService(context.Background(), option.WithHTTPClient(client))
	if err != nil {
		return GoogleReader{}, err
	}

	resp, err := srv.Spreadsheets.Values.BatchGet(sheetID).Ranges(ranges...).Do()
	if err != nil {
		return GoogleReader{}, err
	}

	sheets := make([][][]string, 0, 5)
	for i := 0; i < 5; i++ {
		rows, err := toRows(resp.ValueRanges[i].Values)
		if err != nil {
			return GoogleReader{}, err
		}
		sheets = append(sheets, rows)
	}

	reader := GoogleReader{
		companies: sheets[0],
		customers: sheets[1],
		items:     sheets[2],
		invoices:  sheets[3],
		quotes:    sheets[4],
	}
	return reader, nil
}

func (r GoogleReader) GetCompanies() [][]string {
	return r.companies
}

func (r GoogleReader) GetCustomers() [][]string {
	return r.customers
}

func (r GoogleReader) GetItems() [][]string {
	return r.items
}

func (r GoogleReader) GetInvoices() [][]string {
	return r.invoices
}

func (r GoogleReader) GetQuotes() [][]string {
	return r.quotes
}

func toRows(values [][]any) ([][]string, error) {
	rows := make([][]string, 0, len(values))
	for _, value := range values {
		row := make([]string, 0, len(value))
		for _, v := range value {
			s, ok := v.(string)
			if !ok {
				return nil, fmt.Errorf("could not convert %v to string", v)
			}
			row = append(row, s)
		}
		rows = append(rows, row)
	}

	return rows, nil
}
