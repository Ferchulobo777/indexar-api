package ar.com.ferrodriguez.indexar.web;

/** La serie pedida no existe en el catálogo. Mapeada a 404 por {@link ApiExceptionHandler}. */
public class SeriesNotFoundException extends RuntimeException {

    public SeriesNotFoundException(String code) {
        super("No existe la serie '" + code + "'");
    }
}
