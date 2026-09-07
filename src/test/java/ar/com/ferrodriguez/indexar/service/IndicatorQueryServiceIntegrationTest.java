package ar.com.ferrodriguez.indexar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.com.ferrodriguez.indexar.TestcontainersConfiguration;
import ar.com.ferrodriguez.indexar.domain.IndicatorValue;
import ar.com.ferrodriguez.indexar.repository.IndicatorSeriesRepository;
import ar.com.ferrodriguez.indexar.repository.IndicatorValueRepository;
import ar.com.ferrodriguez.indexar.web.SeriesNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de integración real: levanta un Postgres de verdad (Testcontainers), corre las
 * migraciones de Flyway (incluye el seed de series), y ejercita las queries JPQL
 * custom contra la base real. Esto es lo que valida que el esquema y las queries
 * realmente funcionan juntos — un mock nunca lo hubiera detectado.
 *
 * <p>Todos los {@link Instant} de este test se truncan a microsegundos con
 * {@code truncatedTo(MICROS)}. Postgres {@code timestamptz} guarda microsegundos,
 * pero {@code Instant.now()} trae nanosegundos — sin el truncado, el valor que
 * vuelve de la base difiere del original en los últimos 3 dígitos y el test
 * falla por un problema de precisión, no de lógica.</p>
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class IndicatorQueryServiceIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private IndicatorQueryService queryService;

    @org.springframework.beans.factory.annotation.Autowired
    private IndicatorSeriesRepository seriesRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private IndicatorValueRepository valueRepository;

    @Test
    void elCatalogoSembradoPorFlywayEstaDisponible() {
        var series = queryService.listSeries();

        assertThat(series).extracting("code")
                .contains("dolar-oficial", "dolar-blue", "dolar-mep", "dolar-ccl", "dolar-cripto");
    }

    @Test
    void latestDevuelveElValorMasReciente() {
        var oficial = seriesRepository.findByCode("dolar-oficial").orElseThrow();
        Instant hace2h = Instant.now().truncatedTo(ChronoUnit.MICROS).minus(2, ChronoUnit.HOURS);
        Instant hace1h = Instant.now().truncatedTo(ChronoUnit.MICROS).minus(1, ChronoUnit.HOURS);
        valueRepository.save(new IndicatorValue(
                oficial, hace2h, new BigDecimal("1000.00"), new BigDecimal("1040.00"), Instant.now()));
        valueRepository.save(new IndicatorValue(
                oficial, hace1h, new BigDecimal("1005.00"), new BigDecimal("1045.00"), Instant.now()));

        var latest = queryService.latest("dolar-oficial");

        assertThat(latest.observedAt()).isEqualTo(hace1h);
        assertThat(latest.sellValue()).isEqualByComparingTo("1045.00");
    }

    @Test
    void historyRespetaElRangoDeFechas() {
        var blue = seriesRepository.findByCode("dolar-blue").orElseThrow();
        Instant dentro = Instant.now().truncatedTo(ChronoUnit.MICROS).minus(3, ChronoUnit.HOURS);
        Instant fuera = Instant.now().truncatedTo(ChronoUnit.MICROS).minus(10, ChronoUnit.DAYS);
        valueRepository.save(new IndicatorValue(
                blue, dentro, new BigDecimal("1200.00"), new BigDecimal("1220.00"), Instant.now()));
        valueRepository.save(new IndicatorValue(
                blue, fuera, new BigDecimal("1100.00"), new BigDecimal("1120.00"), Instant.now()));

        var history = queryService.history(
                "dolar-blue", Instant.now().truncatedTo(ChronoUnit.MICROS).minus(1, ChronoUnit.DAYS), Instant.now());

        assertThat(history).hasSize(1);
        assertThat(history.getFirst().observedAt()).isEqualTo(dentro);
    }

    @Test
    void unaSerieInexistenteLanza404DeNegocio() {
        assertThatThrownBy(() -> queryService.latest("dolar-marciano"))
                .isInstanceOf(SeriesNotFoundException.class);
    }
}
