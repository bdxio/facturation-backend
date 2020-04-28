package dev.coop.facturation.model;

public class Adresse {

    private String rue1;
    private String rue2;
    private String rue3;
    private String codePostal;
    private String ville;
    private String pays;

//    private Adresse(String rue1, String rue2, String rue3, String codePostal, String ville, String pays) {
//        this.rue1 = rue1;
//        this.rue2 = rue2;
//        this.rue3 = rue3;
//        this.codePostal = codePostal;
//        this.ville = ville;
//        this.pays = pays;
//    }
    public String getRue1() {
        return rue1;
    }

    public String getRue2() {
        return rue2;
    }

    public String getRue3() {
        return rue3;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public String getVille() {
        return ville;
    }

    public String getPays() {
        return pays;
    }

    public Adresse setRue1(String r1) {
        rue1 = r1;
        return this;
    }

    public Adresse setRue2(String r2) {
        rue2 = r2;
        return this;
    }

    public Adresse setRue3(String r3) {
        rue3 = r3;
        return this;
    }

    public Adresse setCodePostal(String cP) {
        codePostal = cP;
        return this;
    }

    public Adresse setVille(String v) {
        ville = v;
        return this;
    }

    public Adresse setPays(String p) {
        pays = p;
        return this;
    }

}