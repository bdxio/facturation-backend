package dev.coop.facturation.tools;

import java.text.Normalizer;

/**
 *
 * @author lfo
 */
public class Strings {

    private static final String THOUSAND_SEPARATOR = " ";

    public static String startOf(String value, String delimiter) {
        if (value == null) {
            return null;
        }
        final String[] tokens = value.split(delimiter);
        return (tokens.length == 0) ? "" : tokens[0];
    }

    public static String getNumber(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll(THOUSAND_SEPARATOR, "").replaceAll(" ", "").replace("€", "").replace(",", ".");
    }

    public static String alignLeft(String value, int length) {
        String format = "%-" + length + "s";
        return String.format(format, value).substring(0, length - 1);
    }

    public static String alignRight(String value, int length) {
        String format = "%" + length + "s";
        return String.format(format, value);
    }

    public static String decompose(String s) {
        return s == null ? null : Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
