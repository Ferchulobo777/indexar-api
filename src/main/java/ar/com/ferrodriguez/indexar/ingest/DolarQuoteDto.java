package ar.com.ferrodriguez.indexar.ingest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Forma exacta de cada elemento del array que devuelve GET /v1/dolares de dolarapi.com.
 * Ej.: {"casa":"oficial","compra":1005,"venta":1045,"fechaActualizacion":"2026-08-31T..."}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DolarQuoteDto(
        String casa,
        BigDecimal compra,
        BigDecimal venta,
        Instant fechaActualizacion) {

    /** Mapea el código de "casa" de dolarapi.com a nuestro código de serie interno. */
    public String toSeriesCode() {
        return "dolar-" + casa;
    }
}
