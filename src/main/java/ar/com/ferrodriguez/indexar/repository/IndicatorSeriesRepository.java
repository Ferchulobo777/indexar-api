package ar.com.ferrodriguez.indexar.repository;

import ar.com.ferrodriguez.indexar.domain.IndicatorSeries;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndicatorSeriesRepository extends JpaRepository<IndicatorSeries, Long> {

    Optional<IndicatorSeries> findByCode(String code);
}
