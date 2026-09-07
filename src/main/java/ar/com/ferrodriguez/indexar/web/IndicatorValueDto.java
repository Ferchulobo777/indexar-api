package ar.com.ferrodriguez.indexar.web;

import ar.com.ferrodriguez.indexar.domain.IndicatorValue;
import java.math.BigDecimal;
import java.time.Instant;

public record IndicatorValueDto(Instant observedAt, BigDecimal buyValue, BigDecimal sellValue) {

    public static IndicatorValueDto from(IndicatorValue value) {
        return new IndicatorValueDto(value.getObservedAt(), value.getBuyValue(), value.getSellValue());
    }
}
