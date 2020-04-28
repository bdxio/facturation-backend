package dev.coop.facturation;

/**
 *
 * @author lfo
 */
public class FacturationException extends RuntimeException {

    public FacturationException() {
    }

    public FacturationException(String message) {
        super(message);
    }

    public FacturationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FacturationException(Throwable cause) {
        super(cause);
    }
    
    
}
