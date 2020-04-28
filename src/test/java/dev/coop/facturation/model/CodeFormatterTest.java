package dev.coop.facturation.model;

import dev.coop.facturation.model.CodeFormatter.Prefix;
import org.junit.Assert;
import org.junit.Test;

public class CodeFormatterTest {
    
    @Test
    public void testFormatParse() {
        System.out.println("CODE_FORMATTER.format(Prefix.FA, CENT_VINGT_TROIS) = " + CODE_FORMATTER.format(Prefix.FA, CENT_VINGT_TROIS));
        Assert.assertEquals(CENT_VINGT_TROIS, CODE_FORMATTER.parseNumber(CODE_FORMATTER.format(Prefix.FA, CENT_VINGT_TROIS)));
        
    }
    private static final int CENT_VINGT_TROIS = 123;
    private static final CodeFormatter CODE_FORMATTER = CodeFormatter.INSTANCE;
}
