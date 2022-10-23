package domain

import (
	"fmt"
	"strconv"
	"strings"
	"time"
)

type Company struct {
	Name      string
	ShortName string
	Desc      string
	Address   Address
	URL       string
	Email     string
	SIRET     string
	IBAN      string
	BIC       string
	Logo      string
}

type Customer struct {
	ID        string
	Name      string
	ShortName string
	Address   Address
}

type Item struct {
	ID     string
	Desc   string
	Amount int
}

func (i Item) Code() string {
	code := i.ID

	if count := 4 - len(code); count > 0 {
		code = strings.Repeat("0", count) + code
	}

	return "AR" + code
}

func (i Item) Price() string {
	return format(i.Amount)
}

type Invoice struct {
	ID       string
	Company  Company
	Customer Customer
	Date     time.Time
	Lines    []InvoiceLine
}

func (i Invoice) Name() string {
	return fmt.Sprintf("FA%s-%s-%s", i.ID, i.Company.ShortName, i.Customer.ShortName)
}

func (i Invoice) Title() string { return fmt.Sprintf("FA%s", i.ID) }

func (i Invoice) Amount() string {
	sum := 0
	for _, line := range i.Lines {
		sum += line.Qty * line.Item.Amount
	}

	return format(sum)
}

func (i Invoice) Type() string {
	for _, line := range i.Lines {
		if line.Deposit > 0 {
			return "Facture d'acompte"
		}
	}

	return "Facture"
}

type InvoiceLine struct {
	Item    Item
	Qty     int
	Desc    string
	Deposit Percent
}

func (l InvoiceLine) Description() string {
	desc := l.Desc
	if desc == "" {
		desc = l.Item.Desc
	}

	if l.Deposit == 0 {
		return desc
	}

	return fmt.Sprintf("Acompte de %d %% - %s", l.Deposit, desc)
}

func (l InvoiceLine) Amount() string { return format(l.Qty * l.Item.Amount) }

type Quote struct {
	ID       string
	Company  Company
	Customer Customer
	Date     time.Time
	Lines    []QuoteLine
}

func (q Quote) Name() string {
	return fmt.Sprintf("DE%s-%s-%s", q.ID, q.Company.ShortName, q.Customer.ShortName)
}

func (q Quote) Title() string { return fmt.Sprintf("DE%s", q.ID) }

func (q Quote) Type() string {
	return "Devis"
}

func (q Quote) Amount() string {
	sum := 0
	for _, line := range q.Lines {
		sum += line.Qty * line.Item.Amount
	}

	return format(sum)
}

type QuoteLine struct {
	Item Item
	Desc string
	Qty  int
}

func (l QuoteLine) Description() string {
	if l.Desc == "" {
		return l.Item.Desc
	}
	return l.Desc
}

func (l QuoteLine) Amount() string { return format(l.Qty * l.Item.Amount) }

type Address struct {
	Street1 string
	Street2 string
	Street3 string
	ZipCode string
	City    string
}

type Percent int

const moneySuffix = ",00 €"

func format(money int) string {
	if money < 1_000 {
		return fmt.Sprintf("%d%s", money, moneySuffix)
	}

	s := strconv.Itoa(money)

	var b strings.Builder
	b.Grow(len(s) * 2)

	n := len(s) % 3
	if n > 0 {
		b.WriteString(s[:n])
		b.WriteRune(' ')
	}

	rem := s[n:]
	for i, r := range rem {
		b.WriteRune(r)
		if (i+1)%3 == 0 && i != len(rem)-1 {
			b.WriteRune(' ')
		}
	}

	b.WriteString(moneySuffix)

	return b.String()
}
