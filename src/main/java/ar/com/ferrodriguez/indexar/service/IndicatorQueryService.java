package ar.com.ferrodriguez.indexar.service;

import ar.com.ferrodriguez.indexar.domain.IndicatorValue;
import ar.com.ferrodriguez.indexar.repository.IndicatorSeriesRepository;
import ar.com.ferrodriguez.indexar.repository.IndicatorValueRepository;
import ar.com.ferrodriguez.indexar.web.IndicatorValueDto;
import ar.com.ferrodriguez.indexar.web.SeriesNotFoundException;
import ar.com.ferrodriguez.indexar.web.SeriesSummaryDto;
import java.time.Instant;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IndicatorQueryService {

    private final IndicatorSeriesRepository seriesRepository;
    private final IndicatorValueRepository valueRepository;

    public IndicatorQueryService(
            IndicatorSeriesRepository seriesRepository, IndicatorValueRepository valueRepository) {
        this.seriesRepository = seriesRepository;
        this.valueRepository = valueRepository;
    }

    @Cacheable("series-list")
    public List<SeriesSummaryDto> listSeries() {
        return seriesRepository.findAll().stream().map(SeriesSummaryDto::from).toList();
    }

    public IndicatorValueDto latest(String seriesCode) {
        assertSeriesExists(seriesCode);
        return valueRepository
                .findLatest(seriesCode)
                .map(IndicatorValueDto::from)
                .orElseThrow(() -> new SeriesNotFoundException(seriesCode));
    }

    public List<IndicatorValueDto> history(String seriesCode, Instant from, Instant to) {
        assertSeriesExists(seriesCode);
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' no puede ser posterior a 'to'");
        }
        List<IndicatorValue> values = valueRepository.findHistory(seriesCode, from, to);
        return values.stream().map(IndicatorValueDto::from).toList();
    }

    private void assertSeriesExists(String seriesCode) {
        if (seriesRepository.findByCode(seriesCode).isEmpty()) {
            throw new SeriesNotFoundException(seriesCode);
        }
    }
}
