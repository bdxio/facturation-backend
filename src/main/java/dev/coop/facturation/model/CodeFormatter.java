package dev.coop.facturation.model;

import com.google.common.base.Strings;

/**
 *
 * @author lforet
 */
public enum CodeFormatter {

    INSTANCE;

    public enum Prefix {

        FA(8, "Facture"), CL(5, "Client"), AR(5, "Article"), DE(8, "Devis");

        private Prefix(int padding, String description) {
            this.padding = padding;
            this.description = description;
        }

        public int getPadding() {
            return padding;
        }

        public String getDescription() {
            return description;
        }
        
        private final int padding;
        private final String description;
    }

    public String format(Prefix prefix, int num) {
        String number = Strings.padStart(Integer.toString(num), prefix.padding, '0');
        return prefix.name().concat(number);
    }
    
    public int parseNumber(String code) {
        return Integer.parseInt(code.substring(2));
    }
}
