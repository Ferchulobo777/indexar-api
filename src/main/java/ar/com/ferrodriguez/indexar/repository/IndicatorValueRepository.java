package ar.com.ferrodriguez.indexar.repository;

import ar.com.ferrodriguez.indexar.domain.IndicatorValue;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndicatorValueRepository extends JpaRepository<IndicatorValue, Long> {

    @Query("""
            select v from IndicatorValue v
            where v.series.code = :seriesCode
              and v.observedAt between :from and :to
            order by v.observedAt desc
            """)
    List<IndicatorValue> findHistory(
            @Param("seriesCode") String seriesCode, @Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            select v from IndicatorValue v
            where v.series.code = :seriesCode
            order by v.observedAt desc
            limit 1
            """)
    Optional<IndicatorValue> findLatest(@Param("seriesCode") String seriesCode);

    boolean existsBySeriesIdAndObservedAt(Long seriesId, Instant observedAt);
}
