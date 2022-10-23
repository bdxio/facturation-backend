package pdf

import (
	"crypto/sha1"
	"fmt"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"

	"github.com/bdxio/facturation-backend/domain"
)

func TestGenerate_GenerateInvoice(t *testing.T) {
	t.Parallel()

	invoice := domain.Invoice{
		ID: "20220001",
		Company: domain.Company{
			Name:      "Bordeaux Developer eXperience",
			ShortName: "BDXIO",
			Desc:      "BDX I/O",
			Address: domain.Address{
				Street1: "1 rue du Centre",
				Street2: "Bâtiment B",
				Street3: "Etage 3",
				ZipCode: "33000",
				City:    "Bordeaux",
			},
			URL:   "https://www.bdxio.fr",
			Email: "team@bdxio.fr",
			SIRET: "123 456 789 00012",
			IBAN:  "FR 33 30002 00550 0000157841Z 25",
			BIC:   "DAAE FR PP CCT",
			Logo:  "iVBORw0KGgoAAAANSUhEUgAAAFoAAAAyBAMAAAAuIdEGAAAAHlBMVEXMzMyWlpbFxcWxsbGcnJy3t7e+vr6qqqq/v7+jo6M6RxTRAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAAf0lEQVRIiWNgGAWjYBSMbCA0eFQzuwQwsHsmMoBowqoDg8UYTIIdGUA0YdWuDOUMrgyGDCCasGohhhQmIYZEoAdSCKuWEGBQZIJi4sxmINpsEt0NCovAYEEiwwQUzswugcSFNxQQ4WY4mMpgSYJqk1RXElRzihiQoHoUjAIUAACcyxJ5GIahFgAAAABJRU5ErkJggg==",
		},
		Customer: domain.Customer{
			ID:        "1",
			Name:      "A Customer",
			ShortName: "ACustomer",
			Address: domain.Address{
				Street1: "1 Rue du Docteur Goujon",
				Street2: "",
				Street3: "",
				ZipCode: "75012",
				City:    "Paris",
			},
		},
		Date: time.Date(2022, 1, 2, 15, 4, 5, 0, time.UTC),
		Lines: []domain.InvoiceLine{
			{
				Item: domain.Item{ID: "AR00001", Desc: "Description Item which is also very long and won't fit on one line and a word in -ing", Amount: 80},
				Qty:  20,
			},
			{
				Item: domain.Item{ID: "AR00002", Desc: "This description is not very long on purpose", Amount: 40},
				Qty:  1,
			},
			{
				Item: domain.Item{ID: "AR00003", Desc: "An item with a very long description not fitting on one line", Amount: 50},
				Qty:  1,
			},
		},
	}

	data, err := Generator{}.GenerateInvoice(invoice)
	require.NoError(t, err)

	// Checking the size is quite brittle but as some metadata changes between each PDF generation checksum checking
	// is not possible.
	assert.Len(t, data, 3469)
	// The last 128 bytes seems to be consistent between each PDF generation.
	sum := sha1.Sum(data[len(data)-128:])
	assert.Equal(t, "dfc5ce835cfea7ef0aef911a7ba0ab0d994b648e", fmt.Sprintf("%x", sum))
	// These bytes seem also consistent.
	sum = sha1.Sum(data[:1450])
	assert.Equal(t, "90fe9ba5213a39324860bf17dbaf781f051d7ae7", fmt.Sprintf("%x", sum))
}

func TestGenerate_GenerateInvoice_Err(t *testing.T) {
	t.Parallel()

	invoice := domain.Invoice{
		ID: "20220001",
		Company: domain.Company{
			Name:      "Bordeaux Developer eXperience",
			ShortName: "BDXIO",
			Desc:      "BDX I/O",
			Address: domain.Address{
				Street1: "1 rue du Centre",
				Street2: "Bâtiment B",
				Street3: "",
				ZipCode: "33000",
				City:    "Bordeaux",
			},
			URL:   "https://www.bdxio.fr",
			Email: "team@bdxio.fr",
			SIRET: "123 456 789 00012",
			IBAN:  "FR 33 30002 00550 0000157841Z 25",
			BIC:   "DAAE FR PP CCT",
			Logo:  "This is not a valid png logo encoded in base64",
		},
		Customer: domain.Customer{
			ID:        "1",
			Name:      "A Customer",
			ShortName: "ACustomer",
			Address: domain.Address{
				Street1: "1 Rue du Docteur Goujon",
				Street2: "",
				Street3: "",
				ZipCode: "75012",
				City:    "Paris",
			},
		},
		Date: time.Now(),
		Lines: []domain.InvoiceLine{
			{
				Item: domain.Item{ID: "AR00001", Desc: "Description Item which is also very long and won't fit on one line and a word in -ing", Amount: 80},
				Qty:  20,
			},
		},
	}

	_, err := Generator{}.GenerateInvoice(invoice)

	assert.EqualError(t, err, "not a PNG buffer")
}

func TestGenerator_GenerateQuote(t *testing.T) {
	t.Parallel()

	quote := domain.Quote{
		ID: "20220001",
		Company: domain.Company{
			Name:      "Bordeaux Developer eXperience",
			ShortName: "BDXIO",
			Desc:      "BDX I/O",
			Address: domain.Address{
				Street1: "1 rue du Centre",
				Street2: "Bâtiment B",
				Street3: "",
				ZipCode: "33000",
				City:    "Bordeaux",
			},
			URL:   "https://www.bdxio.fr",
			Email: "team@bdxio.fr",
			SIRET: "123 456 789 00012",
			IBAN:  "FR 33 30002 00550 0000157841Z 25",
			BIC:   "DAAE FR PP CCT",
			Logo:  "iVBORw0KGgoAAAANSUhEUgAAAFoAAAAyBAMAAAAuIdEGAAAAHlBMVEXMzMyWlpbFxcWxsbGcnJy3t7e+vr6qqqq/v7+jo6M6RxTRAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAAf0lEQVRIiWNgGAWjYBSMbCA0eFQzuwQwsHsmMoBowqoDg8UYTIIdGUA0YdWuDOUMrgyGDCCasGohhhQmIYZEoAdSCKuWEGBQZIJi4sxmINpsEt0NCovAYEEiwwQUzswugcSFNxQQ4WY4mMpgSYJqk1RXElRzihiQoHoUjAIUAACcyxJ5GIahFgAAAABJRU5ErkJggg==",
		},
		Customer: domain.Customer{
			ID:        "1",
			Name:      "A Customer",
			ShortName: "ACustomer",
			Address: domain.Address{
				Street1: "1 Rue du Docteur Goujon",
				Street2: "",
				Street3: "",
				ZipCode: "75012",
				City:    "Paris",
			},
		},
		Date: time.Date(2022, 1, 2, 15, 4, 5, 0, time.UTC),
		Lines: []domain.QuoteLine{
			{
				Item: domain.Item{ID: "AR00001", Desc: "Description Item which is also very long and won't fit on one line and a word in -ing", Amount: 80},
				Qty:  20,
			},
			{
				Item: domain.Item{ID: "AR00002", Desc: "This description is not very long on purpose", Amount: 40},
				Qty:  1,
			},
			{
				Item: domain.Item{ID: "AR00003", Desc: "An item with a very long description not fitting on one line", Amount: 50},
				Qty:  1,
			},
		},
	}

	data, err := Generator{}.GenerateQuote(quote)
	require.NoError(t, err)

	// Checking the size is quite brittle but as some metadata changes between each PDF generation checksum checking
	// is not possible.
	assert.Len(t, data, 3251)
	// The last 128 bytes seems to be consistent between each PDF generation.
	sum := sha1.Sum(data[len(data)-128:])
	assert.Equal(t, "7ad8040f8b5b7912c7f1337522f8d0cecc18c36f", fmt.Sprintf("%x", sum))
	// These bytes seem also consistent.
	sum = sha1.Sum(data[:1450])
	assert.Equal(t, "d7d762509e49ff4f4ca10bd8926d47f1cf31f3c1", fmt.Sprintf("%x", sum))
}

func TestGenerator_Name(t *testing.T) {
	t.Parallel()

	filename := Generator{}.Filename("filename")

	assert.Equal(t, "filename.pdf", filename)
}

func TestGenerator_MimeType(t *testing.T) {
	t.Parallel()

	assert.Equal(t, "application/pdf", Generator{}.MimeType())
}
