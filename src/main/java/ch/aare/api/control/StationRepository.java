package ch.aare.api.control;

import ch.aare.api.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    List<Station> findAllByNumberIn(List<String> stationNumbers);
}
