package ar.com.ferrodriguez.indexar.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.com.ferrodriguez.indexar.service.IndicatorQueryService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test de la capa web en aislamiento: el servicio va mockeado, esto solo verifica
 * el contrato HTTP (rutas, códigos de estado, forma del JSON) — rápido, sin Spring
 * context completo ni base de datos.
 */
@WebMvcTest(IndicatorController.class)
class IndicatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IndicatorQueryService queryService;

    @Test
    void listSeriesDevuelveElCatalogo() throws Exception {
        when(queryService.listSeries())
                .thenReturn(List.of(new SeriesSummaryDto("dolar-oficial", "Dólar Oficial", "ARS", "dolarapi.com")));

        mockMvc.perform(get("/api/v1/series"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("dolar-oficial"));
    }

    @Test
    void latestDevuelve404ParaSerieInexistente() throws Exception {
        when(queryService.latest("dolar-marciano")).thenThrow(new SeriesNotFoundException("dolar-marciano"));

        mockMvc.perform(get("/api/v1/series/dolar-marciano/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void latestDevuelveElValorMasReciente() throws Exception {
        var value = new IndicatorValueDto(Instant.parse("2026-08-31T10:00:00Z"),
                new BigDecimal("1000.00"), new BigDecimal("1040.00"));
        when(queryService.latest("dolar-oficial")).thenReturn(value);

        mockMvc.perform(get("/api/v1/series/dolar-oficial/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sellValue").value(1040.00));
    }

    @Test
    void historyValidaQueFromNoSeaPosteriorATo() throws Exception {
        when(queryService.history(eq("dolar-oficial"), any(), any()))
                .thenThrow(new IllegalArgumentException("'from' no puede ser posterior a 'to'"));

        mockMvc.perform(get("/api/v1/series/dolar-oficial/history")
                        .param("from", "2026-08-31T10:00:00Z")
                        .param("to", "2026-08-30T10:00:00Z"))
                .andExpect(status().isBadRequest());
    }
}
