package ar.com.ferrodriguez.indexar.ingest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Dispara la ingesta cada 15 minutos. Las cotizaciones de dolarapi.com no cambian con más frecuencia. */
@Component
public class IngestScheduler {

    private static final Logger log = LoggerFactory.getLogger(IngestScheduler.class);

    private final IngestService ingestService;

    public IngestScheduler(IngestService ingestService) {
        this.ingestService = ingestService;
    }

    @Scheduled(fixedRate = 15, initialDelay = 0, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    public void run() {
        try {
            ingestService.ingestOnce();
        } catch (DolarApiException e) {
            // Un fallo de la API externa no debe tirar abajo el scheduler: se reintenta
            // en el próximo ciclo. La API sigue sirviendo los últimos datos persistidos.
            log.warn("Ingesta programada falló, se reintenta en el próximo ciclo: {}", e.getMessage());
        }
    }
}
