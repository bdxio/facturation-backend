package dev.coop.facturation.google;

import dev.coop.facturation.FacturationException;

/**
 *
 * @author lfo
 */
public class GsException extends FacturationException {

    public GsException() {
    }

    public GsException(String message) {
        super(message);
    }

    public GsException(String message, Throwable cause) {
        super(message, cause);
    }

    public GsException(Throwable cause) {
        super(cause);
    }

    
}
