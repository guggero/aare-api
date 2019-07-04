package ch.aare.api.control;

import ch.aare.api.entity.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    Optional<Measurement> findByStation_IdAndUnit_IdAndMeasureTime(Long stationId, Long unitId, LocalDateTime measureTime);

    Optional<Measurement> findFirstByStation_IdAndUnit_IdOrderByMeasureTimeDesc(Long stationId, Long unitId);

    @Modifying
    @Query(value = "INSERT INTO measurementhistory SELECT * FROM measurement WHERE measure_time < :yesterday", nativeQuery = true)
    int moveOldRecords(@Param("yesterday") LocalDateTime yesterday);

    @Modifying
    @Query(value = "DELETE FROM measurement WHERE measure_time < :yesterday", nativeQuery = true)
    int deleteOldRecords(@Param("yesterday") LocalDateTime yesterday);
}
