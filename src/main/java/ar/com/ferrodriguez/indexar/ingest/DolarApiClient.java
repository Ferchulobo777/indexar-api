package ar.com.ferrodriguez.indexar.ingest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cliente HTTP contra la API pública y gratuita de <a href="https://dolarapi.com">dolarapi.com</a>
 * (sin autenticación, sin key). Deliberadamente sin dependencias pesadas (WebClient/Feign):
 * es una sola llamada GET, {@link java.net.http.HttpClient} nativo de Java 11+ alcanza.
 */
@Component
public class DolarApiClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl;

    public DolarApiClient(@Value("${indexar.dolarapi.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Trae todas las cotizaciones vigentes. Lanza {@link DolarApiException} si la API
     * externa falla o responde algo que no se puede parsear — nunca devuelve datos a medias.
     */
    public List<DolarQuoteDto> fetchAllQuotes() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/dolares"))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new DolarApiException("dolarapi.com respondió " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, DolarQuoteDto.class));
        } catch (IOException e) {
            throw new DolarApiException("Error de red consultando dolarapi.com", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DolarApiException("Ingesta interrumpida", e);
        }
    }
}
