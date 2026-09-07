package ar.com.ferrodriguez.indexar.web;

import ar.com.ferrodriguez.indexar.domain.IndicatorSeries;

public record SeriesSummaryDto(String code, String name, String unit, String source) {

    public static SeriesSummaryDto from(IndicatorSeries series) {
        return new SeriesSummaryDto(series.getCode(), series.getName(), series.getUnit(), series.getSource());
    }
}
