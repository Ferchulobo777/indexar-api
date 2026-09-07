package ar.com.ferrodriguez.indexar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Catálogo de series soportadas (una por tipo de dólar / indicador económico).
 * El esquema está pensado para escalar a nuevas fuentes (inflación, tasas,
 * paritarias) sin cambios estructurales.
 */
@Entity
@Table(name = "indicator_series")
public class IndicatorSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 32)
    private String unit;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IndicatorSeries() {
        // JPA
    }

    /** Constructor completo — usado en fixtures de test; en runtime lo crea JPA/Flyway. */
    public IndicatorSeries(Long id, String code, String name, String unit, String source, Instant createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.source = source;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public String getSource() {
        return source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
