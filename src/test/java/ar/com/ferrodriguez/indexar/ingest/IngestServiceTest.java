package ar.com.ferrodriguez.indexar.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.com.ferrodriguez.indexar.domain.IndicatorSeries;
import ar.com.ferrodriguez.indexar.repository.IndicatorSeriesRepository;
import ar.com.ferrodriguez.indexar.repository.IndicatorValueRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test unitario de la orquestación de ingesta — sin red, sin base de datos real:
 * {@link DolarApiClient} y los repositorios están mockeados a propósito para
 * verificar la lógica de negocio (idempotencia, series desconocidas) de forma
 * rápida y determinística.
 */
@ExtendWith(MockitoExtension.class)
class IngestServiceTest {

    @Mock
    private DolarApiClient client;

    @Mock
    private IndicatorSeriesRepository seriesRepository;

    @Mock
    private IndicatorValueRepository valueRepository;

    @Test
    void guardaCadaCotizacionDeUnaSerieConocida() {
        var series = seriesFixture(1L, "dolar-oficial");
        when(client.fetchAllQuotes()).thenReturn(List.of(quote("oficial")));
        when(seriesRepository.findByCode("dolar-oficial")).thenReturn(Optional.of(series));
        when(valueRepository.existsBySeriesIdAndObservedAt(1L, FIXED_INSTANT)).thenReturn(false);

        var service = new IngestService(client, seriesRepository, valueRepository);
        var result = service.ingestOnce();

        assertThat(result.saved()).isEqualTo(1);
        assertThat(result.skippedUnknownSeries()).isZero();
        assertThat(result.skippedDuplicate()).isZero();
        verify(valueRepository).save(any());
    }

    @Test
    void ignoraCotizacionesDeSeriesQueNoEstanEnElCatalogo() {
        when(client.fetchAllQuotes()).thenReturn(List.of(quote("cripto-nueva")));
        when(seriesRepository.findByCode("dolar-cripto-nueva")).thenReturn(Optional.empty());

        var service = new IngestService(client, seriesRepository, valueRepository);
        var result = service.ingestOnce();

        assertThat(result.saved()).isZero();
        assertThat(result.skippedUnknownSeries()).isEqualTo(1);
        verify(valueRepository, never()).save(any());
    }

    @Test
    void reintentarLaMismaIngestaEsIdempotente() {
        var series = seriesFixture(1L, "dolar-oficial");
        when(client.fetchAllQuotes()).thenReturn(List.of(quote("oficial")));
        when(seriesRepository.findByCode("dolar-oficial")).thenReturn(Optional.of(series));
        // ya existe: esto simula la segunda corrida del scheduler sobre el mismo dato
        when(valueRepository.existsBySeriesIdAndObservedAt(1L, FIXED_INSTANT)).thenReturn(true);

        var service = new IngestService(client, seriesRepository, valueRepository);
        var result = service.ingestOnce();

        assertThat(result.saved()).isZero();
        assertThat(result.skippedDuplicate()).isEqualTo(1);
        verify(valueRepository, never()).save(any());
    }

    private static final Instant FIXED_INSTANT = Instant.parse("2026-08-31T12:00:00Z");

    private static DolarQuoteDto quote(String casa) {
        return new DolarQuoteDto(casa, new BigDecimal("1000.00"), new BigDecimal("1050.00"), FIXED_INSTANT);
    }

    private static IndicatorSeries seriesFixture(Long id, String code) {
        return new IndicatorSeries(id, code, "Dólar de prueba", "ARS", "dolarapi.com", FIXED_INSTANT);
    }
}
