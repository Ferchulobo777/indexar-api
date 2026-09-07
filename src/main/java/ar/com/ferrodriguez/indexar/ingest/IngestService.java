package ar.com.ferrodriguez.indexar.ingest;

import ar.com.ferrodriguez.indexar.domain.IndicatorSeries;
import ar.com.ferrodriguez.indexar.domain.IndicatorValue;
import ar.com.ferrodriguez.indexar.repository.IndicatorSeriesRepository;
import ar.com.ferrodriguez.indexar.repository.IndicatorValueRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquesta la ingesta: trae las cotizaciones vigentes de dolarapi.com y persiste
 * solo las series que ya conocemos (catálogo sembrado por V2__seed_series.sql).
 *
 * <p>Idempotente a propósito: la restricción única (series_id, observed_at) en
 * {@link IndicatorValue} hace que reintentar la misma ingesta no duplique filas —
 * {@link #ingestOnce()} puede llamarse tantas veces como haga falta sin efectos
 * secundarios, algo que un scheduler necesita si un job se solapa con el siguiente.</p>
 */
@Service
public class IngestService {

    private static final Logger log = LoggerFactory.getLogger(IngestService.class);

    private final DolarApiClient client;
    private final IndicatorSeriesRepository seriesRepository;
    private final IndicatorValueRepository valueRepository;

    public IngestService(
            DolarApiClient client,
            IndicatorSeriesRepository seriesRepository,
            IndicatorValueRepository valueRepository) {
        this.client = client;
        this.seriesRepository = seriesRepository;
        this.valueRepository = valueRepository;
    }

    @Transactional
    public IngestResult ingestOnce() {
        List<DolarQuoteDto> quotes = client.fetchAllQuotes();
        int saved = 0;
        int skippedUnknownSeries = 0;
        int skippedDuplicate = 0;

        for (DolarQuoteDto quote : quotes) {
            String seriesCode = quote.toSeriesCode();
            var series = seriesRepository.findByCode(seriesCode);
            if (series.isEmpty()) {
                // La API externa puede agregar "casas" nuevas que todavía no dimos de alta
                // en el catálogo (V2__seed_series.sql) — se ignoran, no se inventan series.
                skippedUnknownSeries++;
                continue;
            }

            IndicatorSeries indicatorSeries = series.get();
            if (valueRepository.existsBySeriesIdAndObservedAt(indicatorSeries.getId(), quote.fechaActualizacion())) {
                skippedDuplicate++;
                continue;
            }

            valueRepository.save(new IndicatorValue(
                    indicatorSeries, quote.fechaActualizacion(), quote.compra(), quote.venta(), Instant.now()));
            saved++;
        }

        log.info(
                "Ingesta completa: {} guardados, {} series desconocidas, {} ya existentes",
                saved, skippedUnknownSeries, skippedDuplicate);
        return new IngestResult(saved, skippedUnknownSeries, skippedDuplicate);
    }

    public record IngestResult(int saved, int skippedUnknownSeries, int skippedDuplicate) {
    }
}
