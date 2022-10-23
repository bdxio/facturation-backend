package domain

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestItem_Code(t *testing.T) {
	t.Parallel()

	tests := []struct {
		name string
		item Item
		want string
	}{
		{
			name: "id with 1 character",
			item: Item{ID: "1"},
			want: "AR0001",
		},
		{
			name: "id with 4 characters",
			item: Item{ID: "123"},
			want: "AR0123",
		},
		{
			name: "id with 5 characters",
			item: Item{ID: "12345"},
			want: "AR12345",
		},
	}
	for _, tt := range tests {
		tt := tt
		t.Run(tt.name, func(t *testing.T) {
			t.Parallel()

			code := tt.item.Code()

			assert.Equal(t, tt.want, code)
		})
	}
}

func TestItem_Price(t *testing.T) {
	t.Parallel()

	item := Item{Amount: 5000}

	price := item.Price()

	assert.Equal(t, "5 000,00 €", price)
}

func TestInvoice_Name(t *testing.T) {
	t.Parallel()

	invoice := Invoice{
		ID:       "20220001",
		Company:  Company{ShortName: "Company"},
		Customer: Customer{ShortName: "Customer"},
	}

	name := invoice.Name()

	assert.Equal(t, "FA20220001-Company-Customer", name)
}

func TestInvoice_Title(t *testing.T) {
	t.Parallel()

	invoice := Invoice{ID: "20220001"}

	title := invoice.Title()

	assert.Equal(t, "FA20220001", title)
}

func TestInvoice_Amount(t *testing.T) {
	t.Parallel()

	tests := []struct {
		name    string
		invoice Invoice
		want    string
	}{
		{
			name:    "empty invoice",
			invoice: Invoice{Lines: nil},
			want:    "0,00 €",
		},
		{
			name: "invoice",
			invoice: Invoice{
				Lines: []InvoiceLine{
					{Item: Item{Amount: 80}, Qty: 10},
					{Item: Item{Amount: 50}, Qty: 1},
				},
			},
			want: "850,00 €",
		},
		{
			name: "invoice with big amount",
			invoice: Invoice{
				Lines: []InvoiceLine{
					{Item: Item{Amount: 5000}, Qty: 1},
					{Item: Item{Amount: 80}, Qty: 100},
				},
			},
			want: "13 000,00 €",
		},
		{
			name: "invoice with negative amount",
			invoice: Invoice{
				Lines: []InvoiceLine{
					{Item: Item{Amount: 80}, Qty: 20},
					{Item: Item{Amount: -80}, Qty: 1},
				},
			},
			want: "1 520,00 €",
		},
	}
	for _, tt := range tests {
		tt := tt
		t.Run(tt.name, func(t *testing.T) {
			t.Parallel()

			amount := tt.invoice.Amount()

			assert.Equal(t, tt.want, amount)
		})
	}
}

func TestInvoice_Type(t *testing.T) {
	t.Parallel()

	tests := []struct {
		name    string
		invoice Invoice
		want    string
	}{
		{
			name:    "invoice",
			invoice: Invoice{Lines: []InvoiceLine{{Deposit: 0}}},
			want:    "Facture",
		},
		{
			name:    "invoice with deposit",
			invoice: Invoice{Lines: []InvoiceLine{{Deposit: 100}}},
			want:    "Facture d'acompte",
		},
	}
	for _, tt := range tests {
		tt := tt
		t.Run(tt.name, func(t *testing.T) {
			t.Parallel()

			invoiceType := tt.invoice.Type()

			assert.Equal(t, tt.want, invoiceType)
		})
	}
}

func TestInvoiceLine_Description(t *testing.T) {
	t.Parallel()

	tests := []struct {
		name string
		line InvoiceLine
		want string
	}{
		{
			name: "description in invoice",
			line: InvoiceLine{Item: Item{Desc: "desc item"}, Desc: "desc invoice"},
			want: "desc invoice",
		},
		{
			name: "description in item",
			line: InvoiceLine{Item: Item{Desc: "desc item"}, Desc: ""},
			want: "desc item",
		},
		{
			name: "description with deposit",
			line: InvoiceLine{Item: Item{Desc: "desc item"}, Desc: "desc line", Deposit: 50},
			want: "Acompte de 50 % - desc line",
		},
	}
	for _, tt := range tests {
		tt := tt
		t.Run(tt.name, func(t *testing.T) {
			t.Parallel()

			desc := tt.line.Description()

			assert.Equal(t, tt.want, desc)
		})
	}
}

func TestInvoiceLine_Amount(t *testing.T) {
	t.Parallel()

	line := InvoiceLine{Item: Item{Amount: 80}, Qty: 50}

	amount := line.Amount()

	assert.Equal(t, "4 000,00 €", amount)
}

func TestQuote_Name(t *testing.T) {
	t.Parallel()

	quote := Quote{
		ID:       "20220001",
		Company:  Company{ShortName: "Company"},
		Customer: Customer{ShortName: "Customer"},
	}

	name := quote.Name()

	assert.Equal(t, "DE20220001-Company-Customer", name)
}

func TestQuote_Title(t *testing.T) {
	t.Parallel()

	quote := Quote{ID: "20220001"}

	title := quote.Title()

	assert.Equal(t, "DE20220001", title)
}

func TestQuote_Type(t *testing.T) {
	t.Parallel()

	quoteType := Quote{}.Type()

	assert.Equal(t, "Devis", quoteType)
}

func TestQuote_Amount(t *testing.T) {
	t.Parallel()

	tests := []struct {
		name  string
		quote Quote
		want  string
	}{
		{
			name:  "empty quote",
			quote: Quote{Lines: nil},
			want:  "0,00 €",
		},
		{
			name: "quote",
			quote: Quote{
				Lines: []QuoteLine{
					{Item: Item{Amount: 80}, Qty: 10},
					{Item: Item{Amount: 50}, Qty: 1},
				},
			},
			want: "850,00 €",
		},
		{
			name: "quote with big amount",
			quote: Quote{
				Lines: []QuoteLine{
					{Item: Item{Amount: 5000}, Qty: 1},
					{Item: Item{Amount: 80}, Qty: 100},
				},
			},
			want: "13 000,00 €",
		},
		{
			name: "quote with negative amount",
			quote: Quote{
				Lines: []QuoteLine{
					{Item: Item{Amount: 80}, Qty: 20},
					{Item: Item{Amount: -80}, Qty: 1},
				},
			},
			want: "1 520,00 €",
		},
	}
	for _, tt := range tests {
		tt := tt
		t.Run(tt.name, func(t *testing.T) {
			t.Parallel()

			amount := tt.quote.Amount()

			assert.Equal(t, tt.want, amount)
		})
	}
}

func TestQuoteLine_Description(t *testing.T) {
	t.Parallel()

	tests := []struct {
		name string
		line QuoteLine
		want string
	}{
		{
			name: "description in quote",
			line: QuoteLine{Item: Item{Desc: "desc item"}, Desc: "desc quote"},
			want: "desc quote",
		},
		{
			name: "description in item",
			line: QuoteLine{Item: Item{Desc: "desc item"}, Desc: ""},
			want: "desc item",
		},
	}
	for _, tt := range tests {
		tt := tt
		t.Run(tt.name, func(t *testing.T) {
			t.Parallel()

			desc := tt.line.Description()

			assert.Equal(t, tt.want, desc)
		})
	}
}

func TestQuoteLine_Amount(t *testing.T) {
	t.Parallel()

	line := QuoteLine{Item: Item{Amount: 40}, Qty: 25}

	amount := line.Amount()

	assert.Equal(t, "1 000,00 €", amount)
}

func TestFormat(t *testing.T) {
	t.Parallel()

	tests := []struct {
		name  string
		money int
		want  string
	}{
		{
			name:  "zero",
			money: 0,
			want:  "0,00 €",
		},
		{
			name:  "3 digits",
			money: 123,
			want:  "123,00 €",
		},
		{
			name:  "4 digits",
			money: 1_234,
			want:  "1 234,00 €",
		},
		{
			name:  "10 digits",
			money: 1_234_567_890,
			want:  "1 234 567 890,00 €",
		},
	}
	for _, tt := range tests {
		tt := tt
		t.Run(tt.name, func(t *testing.T) {
			t.Parallel()

			s := format(tt.money)

			assert.Equal(t, tt.want, s)
		})
	}
}
