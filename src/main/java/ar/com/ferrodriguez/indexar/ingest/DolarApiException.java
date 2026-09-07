package ar.com.ferrodriguez.indexar.ingest;

/** Falla al consultar o parsear la respuesta de dolarapi.com. */
public class DolarApiException extends RuntimeException {

    public DolarApiException(String message) {
        super(message);
    }

    public DolarApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
