package pdf

import (
	"fmt"
	"math"
	"strconv"
	"time"

	"github.com/johnfercher/maroto/pkg/color"
	"github.com/johnfercher/maroto/pkg/consts"
	"github.com/johnfercher/maroto/pkg/pdf"
	"github.com/johnfercher/maroto/pkg/props"
	"github.com/jung-kurt/gofpdf"

	"github.com/bdxio/facturation-backend/domain"
)

const (
	sizeSmall  = 8
	sizeNormal = 10
	sizeLarge  = 13
	sizeHuge   = 15
)

var colorGray = color.Color{Red: 200, Green: 200, Blue: 200}

type Generator struct{}

func (g Generator) GenerateInvoice(invoice domain.Invoice) ([]byte, error) {
	data := docData{
		company:  invoice.Company,
		customer: invoice.Customer,
		docType:  invoice.Type(),
		title:    invoice.Title(),
		date:     invoice.Date,
		amount:   invoice.Amount(),
		mapper: func() []row {
			rows := make([]row, 0, len(invoice.Lines))
			for _, line := range invoice.Lines {
				rows = append(rows, row{
					code:   line.Item.Code(),
					desc:   line.Description(),
					qty:    line.Qty,
					price:  line.Item.Price(),
					amount: line.Amount(),
				})
			}

			return rows
		},
	}

	return generate(data)
}

func (g Generator) GenerateQuote(quote domain.Quote) ([]byte, error) {
	data := docData{
		company:  quote.Company,
		customer: quote.Customer,
		docType:  quote.Type(),
		title:    quote.Title(),
		date:     quote.Date,
		amount:   quote.Amount(),
		mapper: func() []row {
			rows := make([]row, 0, len(quote.Lines))
			for _, line := range quote.Lines {
				rows = append(rows, row{
					code:   line.Item.Code(),
					desc:   line.Description(),
					qty:    line.Qty,
					price:  line.Item.Price(),
					amount: line.Amount(),
				})
			}

			return rows
		},
		sign: true,
	}

	return generate(data)
}

func (g Generator) Filename(baseFilename string) string { return fmt.Sprintf("%s.pdf", baseFilename) }

func (g Generator) MimeType() string { return "application/pdf" }

type docData struct {
	company  domain.Company
	customer domain.Customer
	docType  string
	title    string
	date     time.Time
	amount   string
	mapper   mapper
	sign     bool
}

type row struct {
	code   string
	desc   string
	qty    int
	price  string
	amount string
}

type mapper func() []row

func generate(data docData) ([]byte, error) {
	m := pdf.NewMaroto(consts.Portrait, consts.A4)
	m.SetPageMargins(20, 15, 20)
	m.SetDefaultFontFamily(consts.Helvetica)

	// fpdf is the underlying library actually generating the PDF document.
	// It is used later to compute the number of lines of the description of a line.
	fpdf := gofpdf.NewCustom(&gofpdf.InitType{
		OrientationStr: "P",
		UnitStr:        "mm",
		SizeStr:        "A4",
		Size:           gofpdf.SizeType{Wd: 0, Ht: 0},
	})
	fpdf.SetFont(consts.Helvetica, "", sizeNormal)

	m.RegisterHeader(createHeader(m, data.company, data.customer))
	m.Row(35, createInfo(m, data.docType, data.title, data.date))
	m.Row(7, createTableHeader(m))
	createTableContent(m, fpdf, data.mapper)
	m.Row(10, func() {})
	m.Row(5, createTableFooter(m, data.amount))
	if data.sign {
		m.Row(50, func() {
			m.ColSpace(8)
			m.Col(4, func() {
				m.Signature("Bon pour accord", props.Font{Size: sizeNormal})
			})
		})
	}
	m.RegisterFooter(createFooter(m, data.company))

	buf, err := m.Output()
	if err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}

func createHeader(m pdf.Maroto, company domain.Company, customer domain.Customer) func() {
	return func() {
		m.Row(32, func() {
			m.Col(5, func() {
				m.Text(company.Name, props.Text{Size: sizeLarge, Style: consts.Bold})
				m.Text(company.Desc, props.Text{Size: sizeNormal, Top: 7, Style: consts.BoldItalic})
				address(m, company.Address, 12)
			})
			m.ColSpace(2)
			m.Col(5, func() {
				// err will be returned when generating the output.
				_ = m.Base64Image(company.Logo, consts.Png, props.Rect{Center: true, Percent: 80})
			})
		})
		m.Row(60, func() {
			m.Col(5, func() {
				m.Text(fmt.Sprintf("Web : %s", company.URL), props.Text{Size: sizeSmall, Top: 2})
				m.Text(fmt.Sprintf("Email : %s", company.Email), props.Text{Size: sizeSmall, Top: 6})
				m.Text(fmt.Sprintf("SIRET : %s", company.SIRET), props.Text{Size: sizeSmall, Top: 10})
			})
			m.ColSpace(2)
			m.Col(5, func() {
				m.Text(customer.Name, props.Text{Size: sizeLarge, Top: 12, Style: consts.Bold, Extrapolate: true})
				address(m, customer.Address, 24)
			})
		})
	}
}

func address(m pdf.Maroto, address domain.Address, top float64) {
	m.Text(address.Street1, props.Text{Size: sizeNormal, Top: top, Extrapolate: true})
	top += 5
	if address.Street2 != "" {
		m.Text(address.Street2, props.Text{Size: sizeNormal, Top: top, Extrapolate: true})
		top += 5
	}
	if address.Street3 != "" {
		m.Text(address.Street3, props.Text{Size: sizeNormal, Top: top, Extrapolate: true})
		top += 5
	}
	m.Text(
		fmt.Sprintf("%s %s", address.ZipCode, address.City),
		props.Text{Size: sizeNormal, Top: top, Extrapolate: true},
	)
}

func createInfo(m pdf.Maroto, docType, title string, date time.Time) func() {
	return func() {
		m.Col(4, func() {
			m.Text(docType, props.Text{Size: sizeHuge, Style: consts.Bold})
			m.Text("Date :", props.Text{Size: sizeNormal, Top: 8, Style: consts.Bold})
			m.Text("Date Règlement :", props.Text{Size: sizeNormal, Top: 13, Style: consts.Bold})
		})
		m.Col(8, func() {
			m.Text(title, props.Text{Size: sizeHuge, Style: consts.Bold})
			m.Text(date.Format("02/01/2006"), props.Text{Size: sizeNormal, Top: 8, Style: consts.Bold})
			m.Text("À réception de la facture", props.Text{Size: sizeNormal, Top: 13, Style: consts.Bold})
		})
	}
}

func createTableHeader(m pdf.Maroto) func() {
	return func() {
		m.SetBackgroundColor(colorGray)
		m.Col(1, func() {
			m.Text("Code", props.Text{Style: consts.Bold, Size: sizeLarge, Align: consts.Center})
		})
		m.Col(6, func() {
			m.Text("Description", props.Text{Style: consts.Bold, Size: sizeLarge, Align: consts.Center})
		})
		m.Col(1, func() {
			m.Text("Qté", props.Text{Style: consts.Bold, Size: sizeLarge, Align: consts.Center})
		})
		m.Col(2, func() {
			m.Text("P.U.", props.Text{Style: consts.Bold, Size: sizeLarge, Align: consts.Center})
		})
		m.Col(2, func() {
			m.Text("Montant", props.Text{Style: consts.Bold, Size: sizeLarge, Align: consts.Center})
		})
		m.SetBackgroundColor(color.NewWhite())
	}
}

func createTableContent(m pdf.Maroto, fpdf *gofpdf.Fpdf, mapper mapper) {
	rows := mapper()
	for _, row := range rows {
		// The PDF library doesn't currently expand a row when a text spans on more than one line, so we compute
		// manually the number of lines the description will take to compute the height of the row.
		// The width of the cell containing the description has been scientifically (*) calculated to the value of 85.
		// (*) using the debugger and getting the Cell.Width...
		nbLines := math.Ceil(fpdf.GetStringWidth(row.desc) / 85)
		rowHeight := 5 * nbLines
		m.Row(rowHeight, func() {
			m.Col(1, func() {
				m.Text(row.code, props.Text{Size: sizeNormal})
			})
			m.Col(6, func() {
				m.Text(row.desc, props.Text{Size: sizeNormal})
			})
			m.Col(1, func() {
				m.Text(strconv.Itoa(row.qty), props.Text{Size: sizeNormal, Align: consts.Right})
			})
			m.Col(2, func() {
				m.Text(row.price, props.Text{Size: sizeNormal, Align: consts.Right})
			})
			m.Col(2, func() {
				m.Text(row.amount, props.Text{Size: sizeNormal, Align: consts.Right})
			})
		})
	}
}

func createTableFooter(m pdf.Maroto, amount string) func() {
	return func() {
		m.Col(8, func() {
			m.Text("TVA non applicable, art. 293 B du CGI", props.Text{Size: sizeNormal, Style: consts.Italic})
		})
		m.SetBackgroundColor(colorGray)
		m.Col(2, func() {
			m.Text("Total", props.Text{Size: sizeNormal, Style: consts.Bold, Align: consts.Right})
		})
		m.Col(2, func() {
			m.Text(amount, props.Text{Size: sizeNormal, Style: consts.Bold, Align: consts.Right})
		})
		m.SetBackgroundColor(color.NewWhite())
	}
}

func createFooter(m pdf.Maroto, company domain.Company) func() {
	return func() {
		m.Row(6, func() {
			m.Col(12, func() {
				m.Text("Règlement par chèque à l'ordre de :", props.Text{Size: sizeLarge})
			})
		})
		m.Row(6, func() {
			m.Col(12, func() {
				m.Text(fmt.Sprintf("    %s", company.Name), props.Text{Size: sizeLarge, Style: consts.Bold})
			})
		})
		m.Row(6, func() {
			m.Col(12, func() {
				m.Text("Par virement :", props.Text{Size: sizeLarge})
			})
		})
		m.Row(6, func() {
			m.Text(fmt.Sprintf("    IBAN : %s", company.IBAN), props.Text{Size: sizeLarge, Style: consts.Bold})
		})
		m.Row(7, func() {
			m.Text(fmt.Sprintf("    BIC : %s", company.BIC), props.Text{Size: sizeLarge, Style: consts.Bold})
		})
		m.Row(5, func() {
			m.Text("Pénalités de retard : 3 fois le taux d'intérêt légal.", props.Text{Size: sizeNormal})
		})
		m.Row(5, func() {
			m.Text("Indemnité forfaitaire pour frais de recouvrement en cas de retard de paiement : 40€", props.Text{Size: sizeNormal})
		})
	}
}
