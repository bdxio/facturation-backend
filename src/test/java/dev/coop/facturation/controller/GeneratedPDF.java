package dev.coop.facturation.controller;

import java.util.Objects;

class GeneratedPDF {
    final String name;
    final String hash;

    GeneratedPDF(String name, String hash) {
        this.name = name;
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "GeneratedPDF{" +
                "name='" + name + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneratedPDF that = (GeneratedPDF) o;
        return name.equals(that.name) && hash.equals(that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, hash);
    }
}
