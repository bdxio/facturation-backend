package dev.coop.facturation.tools;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author lfo
 */
public class Dates {

    public static LocalDate parse(String value) {
        if (value == null) {
            return null;
        }
        return LocalDate.parse(value, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
