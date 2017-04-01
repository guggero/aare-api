package ch.illubits.api.control;

import ch.illubits.api.BaseRepository;
import ch.illubits.api.entity.Measurement;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class MeasurementRepository extends BaseRepository<Measurement> {

    public Measurement findByStationAndUnitAndMeasureTime(Long stationId, Long unitId, LocalDateTime measureTime) {
        return singleResult(
                createNamedQuery("Measurement.findByStationAndUnitAndMeasureTime")
                        .setParameter("stationId", stationId)
                        .setParameter("unitId", unitId)
                        .setParameter("measureTime", measureTime)
        );
    }
}
