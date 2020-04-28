package dev.coop.facturation.tools;

import java.math.BigDecimal;

/**
 *
 * @author lfo
 */
public class Numbers {

    public static Integer toInt(String value) {
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value);
    }

    public static BigDecimal toBigDecimal(String value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(Strings.getNumber(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
