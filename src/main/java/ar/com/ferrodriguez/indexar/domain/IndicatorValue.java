package ar.com.ferrodriguez.indexar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Un valor observado de una serie en un instante dado. La restricción de
 * unicidad (series + observed_at) hace el ingest idempotente: reintentar
 * la misma ingesta nunca duplica una fila.
 */
@Entity
@Table(
        name = "indicator_value",
        uniqueConstraints = @UniqueConstraint(columnNames = {"series_id", "observed_at"}))
public class IndicatorValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "series_id", nullable = false)
    private IndicatorSeries series;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    @Column(name = "buy_value", precision = 14, scale = 4)
    private BigDecimal buyValue;

    @Column(name = "sell_value", nullable = false, precision = 14, scale = 4)
    private BigDecimal sellValue;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    protected IndicatorValue() {
        // JPA
    }

    public IndicatorValue(IndicatorSeries series, Instant observedAt, BigDecimal buyValue,
                           BigDecimal sellValue, Instant fetchedAt) {
        this.series = series;
        this.observedAt = observedAt;
        this.buyValue = buyValue;
        this.sellValue = sellValue;
        this.fetchedAt = fetchedAt;
    }

    public Long getId() {
        return id;
    }

    public IndicatorSeries getSeries() {
        return series;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public BigDecimal getBuyValue() {
        return buyValue;
    }

    public BigDecimal getSellValue() {
        return sellValue;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }
}
