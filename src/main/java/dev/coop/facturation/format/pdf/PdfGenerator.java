package dev.coop.facturation.format.pdf;

import dev.coop.facturation.FacturationException;
import dev.coop.facturation.format.Coord;
import dev.coop.facturation.format.Style;
import dev.coop.facturation.model.Client;
import dev.coop.facturation.model.Facture;
import dev.coop.facturation.model.Societe;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

public abstract class PdfGenerator {

    private static final int MARGIN_LEFT = 20;
    private static final int MARGIN_UP = 20;

    private static final Coord TOP_LEFT_COORD = Coord.createA4().incrX(MARGIN_LEFT).decrY(MARGIN_UP).immutable();
    private static final Coord SOCIETE_COORD = TOP_LEFT_COORD.immutable();
    private static final Coord LOGO_COORD = new Coord(140, 260).immutable();
    private static final String LOGO_NAME = "Logo";
    private static final Coord CLIENT_COORD = new Coord(120, 230).immutable();
    private static final Coord FACTURE_COORD = new Coord(MARGIN_LEFT, 190).immutable();
    private static final Coord TABLEAU_COORD = new Coord(MARGIN_LEFT, 150).immutable();

    private static final Coord TOTAL_COORD = new Coord(MARGIN_LEFT, 90).immutable();
    private static final Coord INFO_ADMIN_COORD = new Coord(MARGIN_LEFT, 20).immutable();
    private static final Coord COORD_BANCAIRE_COORD = new Coord(MARGIN_LEFT, 60).immutable();

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void generate(Facture facture, File folder) throws IOException {
        File file = new File(folder, getFileName(facture));
        generate(facture, new FileOutputStream(file));
    }

    public static String getFileName(Facture facture) {
        return String.format("%s-%s-%s.pdf", facture.getCode(), facture.getSociete().getNomCourt(), facture.getClient().getNomCourt());
    }
    
    public void generate(Facture facture, OutputStream out) {

        PdfBuilder pdf = new PdfBuilder(TOP_LEFT_COORD.copy(), Style.createHelvetica());

        insertLogo(pdf, facture.getSociete());
        insertSociete(pdf, facture.getSociete());
        insertClient(pdf, facture.getClient());
        insertFactureInfos(pdf, facture);
        insertTableauLigne(pdf, facture);
        insertTotaux(pdf, facture);
        insertCoordBancaire(pdf, facture.getSociete());
        insertSocieteInfoAdmin(pdf, facture.getSociete());

        try (var doc = pdf.toDocument()){
            doc.save(out);
        } catch (IOException ex) {
            throw new FacturationException(ex);
        }
    }

    protected void insertLogo(PdfBuilder pdf, Societe societe) {
        pdf.setCoord(LOGO_COORD.copy()).putImage(societe.getLogo(), 50, 30, LOGO_NAME);
    }

    protected void insertSociete(PdfBuilder pdf, Societe societe) {
        pdf.setCoord(SOCIETE_COORD.copy());
        pdf.style().bold().large();
        pdf.println(societe.getNom());
        pdf.style().italic().normal();

        pdf.println(societe.getDescription());
        pdf.style().unbold().unitalic();
        pdf.println(societe.getAdresse().getRue1())
                .printlnIfNotNull(societe.getAdresse().getRue2())
                .printlnIfNotNull(societe.getAdresse().getRue3())
                .println(societe.getAdresse().getCodePostal() + " " + societe.getAdresse().getVille());
        pdf.style().small();
        pdf.coord().decrY(2);
        if (societe.getTel() != null) {
            pdf.println("Tel : ".concat(societe.getTel()));
        }
        if (societe.getWeb() != null) {
            pdf.println("Web : ".concat(societe.getWeb()));
        }
        if (societe.getEmail() != null) {
            pdf.println("Email : ".concat(societe.getEmail()));
        }
    }

    protected void insertClient(PdfBuilder pdf, Client client) {
        pdf.setCoord(CLIENT_COORD.copy());
        pdf.style().large().bold();
        pdf.println(client.getNom());
        pdf.style().normal().unbold();
        pdf.println(client.getAdresse().getRue1())
                .printlnIfNotNull(client.getAdresse().getRue2())
                .printlnIfNotNull(client.getAdresse().getRue3())
                .println(client.getAdresse().getCodePostal() + " " + client.getAdresse().getVille());
        if (client.getAdresse().getPays() != null && !client.getAdresse().getPays().equalsIgnoreCase("FRANCE")) {
            pdf.println(client.getAdresse().getPays().toUpperCase());
        }
        final String tvaIntracom = client.getNumTvaIntracom() == null ? null : "N° TVA : " + client.getNumTvaIntracom();
        pdf.println().printlnIfNotNull(tvaIntracom);
    }

    protected void insertFactureInfos(PdfBuilder pdf, Facture facture) {
        pdf.setCoord(FACTURE_COORD.copy());
        PdfTableBuilder headerTable = pdf.createTableBuilder(new int[]{50, 100});
        pdf.style().huge().bold();
        headerTable.printCell(facture.getCodePrefix().getDescription())
                .printCell(facture.getCode());
        pdf.style().normal();
        headerTable.printCell("Date : ").printCell(facture.getDate().format(DATE_FORMAT));
        if (facture.getSociete().getDelaiPaiement() > 0) {
            headerTable.printCell("Date échéance : ").printCell(facture.getDateEcheance().format(DATE_FORMAT));
        } else {
            headerTable.printCell("Date Règlement :").printCell("À réception de la facture");
        }
        pdf.style().unbold();
    }

    protected void insertTableauLigne(PdfBuilder pdf, Facture facture) {
        pdf.setCoord(TABLEAU_COORD.copy());
        final PdfTableBuilder tableauLignes = pdf.createTableBuilder(new int[]{15, 80, 8, 13, 12, 20, 22});
        pdf.style().bold().setColor(Color.DARK_GRAY).setBackgroundColor(Color.LIGHT_GRAY);
        tableauLignes.printCell("Code", Style.Align.CENTER).printCell("Description", Style.Align.CENTER)
                .printCell("Qté", Style.Align.CENTER)
                .printCell("Unité", Style.Align.CENTER)
                .printCell("TVA", Style.Align.CENTER)
                .printCell("P.U. HT", Style.Align.CENTER)
                .printCell("Montant HT", Style.Align.CENTER);
        pdf.style().unbold().setColor(Color.BLACK).setBackgroundColor(null).small();
        facture.getLignes().stream().forEach((ligne) -> {
//            pdf.coord().setX(MARGIN_LEFT).decrY(pdf.style());
            tableauLignes.printCell(ligne.getArticle().getCode())
                    .printCell(ligne.getDescription())
                    .printCell(ligne.getQuantite() == null ? "" : ligne.getQuantite().toPlainString(), Style.Align.RIGHT)
                    .printCell(ligne.getUnite() == null ? "" : ligne.getUnite().name() , Style.Align.CENTER)
                    .printCell(ligne.getTva().getPercentage().toPlainString(), Style.Align.RIGHT)
                    .printCell(ligne.getMontantUnitaire().toString(), Style.Align.RIGHT)
                    .printCell(ligne.getMontantHT().toString(), Style.Align.RIGHT);
        });
    }

    protected void insertTotaux(PdfBuilder pdf, Facture facture) {
        pdf.setCoord(TOTAL_COORD.copy());

        pdf.style().normal().bold().setColor(Color.DARK_GRAY);
        PdfTableBuilder totalTable = pdf.createTableBuilder(new int[]{130, 20, 20});
        totalTable.printCell("");
        pdf.style().setBackgroundColor(Color.LIGHT_GRAY);
        totalTable.printCell("Total HT");
        pdf.style().setBackgroundColor(Color.WHITE);
        totalTable.printCell(facture.getTotalHT().toString(), Style.Align.RIGHT);
        totalTable.printCell("");
        pdf.style().setBackgroundColor(Color.LIGHT_GRAY);
        totalTable.printCell("Total TVA");
        pdf.style().setBackgroundColor(Color.WHITE);
        totalTable.printCell(facture.getTotalTva().toString(), Style.Align.RIGHT);
        pdf.coord().decrY(2);

        pdf.drawLine(pdf.coord(), pdf.coord().copy().setX(190));
        pdf.println();
        pdf.coord().setX(MARGIN_LEFT);

        final PdfTableBuilder ttcTable = pdf.createTableBuilder(new int[]{130, 20, 20}).printCell("");
        pdf.style().setBackgroundColor(Color.LIGHT_GRAY);
        ttcTable.printCell("Total TTC");
        pdf.style().setBackgroundColor(Color.WHITE);
        ttcTable.printCell(facture.getTotalTtc().toString(), Style.Align.RIGHT);
    }

    protected void insertSocieteInfoAdmin(PdfBuilder pdf, Societe societe) {
        pdf.drawLine(INFO_ADMIN_COORD, INFO_ADMIN_COORD.copy().setX(190));
        pdf.setCoord(INFO_ADMIN_COORD.copy());
        pdf.style().unbold().small().setColor(Color.DARK_GRAY);
        pdf.println();
        pdf.coord().setX(50);
        final String capital = societe.getCapital() == null ? "" : "  Capital : " + societe.getCapital().toString();
        final String siret = societe.getSiret() == null ? "" : "SIRET : " + societe.getSiret();
        final String naf = societe.getNaf() == null ? "" : "  Naf : " + societe.getNaf();
        final String tvaIntracom = societe.getNumTvaIntracom() == null ? "" : "  N° TVA : " + societe.getNumTvaIntracom();
        pdf.println(siret + naf + tvaIntracom + capital)
                .println();
        pdf.coord().setX(MARGIN_LEFT);

    }

    private void insertCoordBancaire(PdfBuilder pdf, Societe societe) {
        pdf.setCoord(COORD_BANCAIRE_COORD.copy());
        pdf.style().reset().large();
        pdf.println("Réglement par chèque à l'ordre de : ");
        pdf.style().bold();
        pdf.println(String.format("    %s", societe.getNom()));

        pdf.style().unbold();
        pdf.println("Par virement : ");
        pdf.style().bold();
        pdf.println(String.format("    IBAN : %s", societe.getIban() == null ? "" : societe.getIban()))
                .println(String.format("    BIC : %s", societe.getBic() == null ? "" : societe.getBic()));
        
        pdf.style().reset();
        pdf.println("Pénalités de retard: 3 fois le taux d'intérêt légal.")
                .println("Indemnité forfaitaire pour frais de recouvrement en cas de retard de paiement: 40€");

    }
    
    @Component
    public static class Default extends PdfGenerator {}

    @Component
    public static class ForAssociation extends PdfGenerator {

        @Override
        protected void insertTotaux(PdfBuilder pdf, Facture facture) {
            pdf.setCoord(TOTAL_COORD.copy());

            pdf.style().large();
            final PdfTableBuilder ttcTable = pdf.createTableBuilder(new int[]{120, 25, 25})
                    .printCell("\"TVA non applicable, article 293 B du Code général des impôts\"");
            pdf.style().setBackgroundColor(Color.LIGHT_GRAY);
            ttcTable.printCell("Total");
            pdf.style().setBackgroundColor(Color.WHITE).bold();
            ttcTable.printCell(facture.getTotalTtc().toString(), Style.Align.RIGHT);

        }

        @Override
        protected void insertTableauLigne(PdfBuilder pdf, Facture facture) {
            pdf.setCoord(TABLEAU_COORD.copy());
            final PdfTableBuilder tableauLignes = pdf.createTableBuilder(new int[]{18, 90, 11,  26, 28});
            pdf.style().bold().setColor(Color.DARK_GRAY).setBackgroundColor(Color.LIGHT_GRAY).large();
            tableauLignes.printCell("Code", Style.Align.CENTER).printCell("Description", Style.Align.CENTER)
                    .printCell("Qté", Style.Align.CENTER)
                    .printCell("P.U.", Style.Align.CENTER)
                    .printCell("Montant", Style.Align.CENTER);
            pdf.style().unbold().setColor(Color.BLACK).setBackgroundColor(Color.WHITE).normal();
            facture.getLignes().stream().forEach((ligne) -> {
                tableauLignes.printCell(ligne.getArticle() != null ? ligne.getArticle().getCode() :"")
                        .printCell(ligne.getDescription())
                        .printCell(ligne.getQuantite().toPlainString(), Style.Align.RIGHT)
                        .printCell(ligne.getMontantUnitaire().toString(), Style.Align.RIGHT)
                        .printCell(ligne.getMontantHT().toString(), Style.Align.RIGHT);
            });
        }

        @Override
        protected void insertSocieteInfoAdmin(PdfBuilder pdf, Societe societe) {
            //super.insertSocieteInfoAdmin(pdf, societe);
        }

        @Override
        protected void insertSociete(PdfBuilder pdf, Societe societe) {
            super.insertSociete(pdf, societe);
            if (societe.getSiret() != null) {
                pdf.println("SIRET : " + societe.getSiret());
            }
        }

    }
}
