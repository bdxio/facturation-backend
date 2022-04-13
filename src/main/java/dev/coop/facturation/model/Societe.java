package dev.coop.facturation.model;

import lombok.ToString;

@ToString
public class Societe {

    private String nom;
    private String nomCourt;
    private String description;
    private String formeJuridique;
    private Adresse adresse;
    private String tel;
    private String fax;
    private String web;
    private String email;
    private String siret;
    private String naf;
    private String numTvaIntracom;
    private Montant capital;
    private String iban;
    private String bic;
    private byte[] logo;
    private Integer delaiPaiement = 45;

    public String getNom() {
        return nom;
    }

    public String getNomCourt() {
        return nomCourt == null ? getNom() : nomCourt;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public String getTel() {
        return tel;
    }

    public String getFax() {
        return fax;
    }

    public String getWeb() {
        return web;
    }

    public String getEmail() {
        return email;
    }

    public String getDescription() {
        return description;
    }

    public String getFormeJuridique() {
        return formeJuridique;
    }

    public String getSiret() {
        return siret;
    }

    public String getNaf() {
        return naf;
    }

    public String getNumTvaIntracom() {
        return numTvaIntracom;
    }

    public Montant getCapital() {
        return capital;
    }

    public byte[] getLogo() {
        return logo;
    }

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }

    public Integer getDelaiPaiement() {
        return delaiPaiement;
    }

    public Societe setDelaiPaiement(Integer delaiPaiement) {
        this.delaiPaiement = delaiPaiement;
        return this;
    }

    public Societe setNom(String n) {
        nom = n;
        return this;
    }

    public Societe setNomCourt(String nomCourt) {
        this.nomCourt = nomCourt;
        return this;
    }

    public Societe setAdresse(Adresse ad) {
        adresse = ad;
        return this;
    }

    public Societe setTel(String tel) {
        this.tel = tel;
        return this;
    }

    public Societe setFax(String fax) {
        this.fax = fax;
        return this;
    }

    public Societe setWeb(String web) {
        this.web = web;
        return this;
    }

    public Societe setMail(String mail) {
        this.email = mail;
        return this;
    }

    public Societe setDescription(String description) {
        this.description = description;
        return this;
    }

    public Societe setFormeJuridique(String formeJuridique) {
        this.formeJuridique = formeJuridique;
        return this;
    }

    public Societe setSiret(String s) {
        siret = s;
        return this;
    }

    public Societe setNaf(String n) {
        naf = n;
        return this;
    }

    public Societe setNumTvaIntracom(String num) {
        numTvaIntracom = num;
        return this;
    }

    public Societe setCapital(Montant cap) {
        capital = cap;
        return this;
    }

    public Societe setLogo(byte[] logo) {
        this.logo = logo;
        return this;
    }

    public Societe setIban(String iban) {
        this.iban = iban;
        return this;
    }

    public Societe setBic(String bic) {
        this.bic = bic;
        return this;
    }

    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Societe) && ((Societe) obj).getNom().equals(getNom());
    }

    @Override
    public int hashCode() {
        return getNom().hashCode();
    }
    
     protected static final int DEFAULT_DELAI_PAIEMENT = 45;
}
