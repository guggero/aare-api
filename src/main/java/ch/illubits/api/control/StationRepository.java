package ch.illubits.api.control;

import ch.illubits.api.BaseRepository;
import ch.illubits.api.entity.Station;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class StationRepository extends BaseRepository<Station> {

    public List<Station> getStations(List<String> stationNumbers) {
        return resultList(createNamedQuery("Station.findByStationNumbers").setParameter("stationNumbers", stationNumbers));
    }
}
