package ar.com.ferrodriguez.indexar.web;

import ar.com.ferrodriguez.indexar.service.IndicatorQueryService;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API pública de indicadores económicos de Argentina — hoy cotizaciones del dólar
 * (dolarapi.com), con el esquema pensado para sumar más series sin romper el contrato.
 */
@RestController
@RequestMapping("/api/v1")
public class IndicatorController {

    private final IndicatorQueryService queryService;

    public IndicatorController(IndicatorQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/series")
    public List<SeriesSummaryDto> listSeries() {
        return queryService.listSeries();
    }

    @GetMapping("/series/{code}/latest")
    public IndicatorValueDto latest(@PathVariable String code) {
        return queryService.latest(code);
    }

    @GetMapping("/series/{code}/history")
    public List<IndicatorValueDto> history(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return queryService.history(code, from, to != null ? to : Instant.now());
    }
}
