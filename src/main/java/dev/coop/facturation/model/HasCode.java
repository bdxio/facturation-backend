package dev.coop.facturation.model;

/**
 *
 * @author lfo
 */
public abstract class HasCode {

    protected String code;
    
    public final String getCode() {
        return code;
    }

    public void setCode(int codeValue) {
        this.code = CodeFormatter.INSTANCE.format(getCodePrefix(), codeValue);
    }
     
    public abstract CodeFormatter.Prefix getCodePrefix();
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof HasCode) && ((HasCode) obj).getCode().equals(getCode());
    }

    @Override
    public int hashCode() {
        return getCode().hashCode();
    }
    
}
