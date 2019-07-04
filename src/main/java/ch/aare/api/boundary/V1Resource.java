package ch.aare.api.boundary;

import ch.aare.api.StringUtils;
import ch.aare.api.control.CollectorService;
import ch.aare.api.control.MeasurementRepository;
import ch.aare.api.control.StationRepository;
import ch.aare.api.control.UnitService;
import ch.aare.api.entity.CurrentMeasurementsContainer;
import ch.aare.api.entity.Measurement;
import ch.aare.api.entity.Station;
import ch.aare.api.entity.Unit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.aare.api.control.UnitService.*;
import static java.util.Comparator.comparing;
import static java.util.regex.Pattern.*;
import static org.springframework.http.HttpHeaders.USER_AGENT;

@RestController
@RequestMapping("/v1")
public class V1Resource {

    private static final Pattern USER_AGENT_PATTERN = Pattern.compile("aare/([\\d\\.a-z]+) .*", CASE_INSENSITIVE | DOTALL | MULTILINE);
    private static final String CAN_HANDLE_NULL_VERSION = "1.6.2";

    private final MeasurementRepository measurementRepository;
    private final StationRepository stationRepository;
    private final UnitService unitService;

    public V1Resource(MeasurementRepository measurementRepository, StationRepository stationRepository,
                      UnitService unitService) {
        this.measurementRepository = measurementRepository;
        this.stationRepository = stationRepository;
        this.unitService = unitService;
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentMeasurements(@RequestHeader(USER_AGENT) String userAgent) {
        List<Station> stations = stationRepository.findAll();
        stations.sort(comparing(Station::getName));
        List<CurrentMeasurementsContainer> currentMeasurements = new ArrayList<>();
        boolean canHandleNull = canHandleNullValues(userAgent);

        stations.forEach(station -> {
            Measurement temperature = findMeasurement(station, UNIT_TEMPERATURE);
            Measurement waterLevel = findMeasurement(station, UNIT_WATER_LEVEL);
            Measurement discharge = findMeasurement(station, UNIT_RUNOFF);

            LocalDateTime measurementTimeStamp = null;
            if (temperature != null) {
                measurementTimeStamp = temperature.getMeasureTime();
            }
            if (measurementTimeStamp == null && waterLevel != null) {
                measurementTimeStamp = waterLevel.getMeasureTime();
            }
            if (measurementTimeStamp == null && discharge != null) {
                measurementTimeStamp = discharge.getMeasureTime();
            }

            CurrentMeasurementsContainer container = new CurrentMeasurementsContainer();
            container.setName(station.getName());
            container.setNumber(station.getNumber());
            container.setDate(canHandleNull(canHandleNull, measurementTimeStamp));
            container.setTemperature(getValue(temperature, canHandleNull));
            container.setWaterlevel(getValue(waterLevel, canHandleNull));
            container.setDischarge(getValue(discharge, canHandleNull));
            container.setTemperature24hAgo(getValue24hAgo(temperature, canHandleNull));
            container.setWaterlevel24hAgo(getValue24hAgo(waterLevel, canHandleNull));
            container.setDischarge24hAgo(getValue24hAgo(discharge, canHandleNull));
            currentMeasurements.add(container);
        });

        return ResponseEntity.ok(new TreeMap<String, Object>() {{
            put("date", LocalDateTime.now());
            put("dateformat", "unix");
            put("source", "BAFU");
            put("stations", currentMeasurements);
        }});
    }

    private Float getValue(Measurement measurement, boolean canHandleNull) {
        if (measurement == null || measurement.getValue() == null) {
            if (canHandleNull) {
                return null;
            } else {
                return 0.0f;
            }
        } else {
            return measurement.getValue();
        }
    }

    private Float getValue24hAgo(Measurement measurement, boolean canHandleNull) {
        if (measurement == null || measurement.getValue24h() == null) {
            if (canHandleNull) {
                return null;
            } else {
                return 0.0f;
            }
        } else {
            return measurement.getValue24h();
        }
    }

    private Measurement findMeasurement(Station station, String unitKey) {
        Unit unit = unitService.getUnit(unitKey);
        if (unit == null || unit.getId() == null) {
            throw new RuntimeException("Could not load unit for key '" + unitKey + "'");
        }

        return measurementRepository.findFirstByStation_IdAndUnit_IdOrderByMeasureTimeDesc(station.getId(), unit.getId())
            .orElse(null);
    }

    private static LocalDateTime canHandleNull(boolean canHandleNull, LocalDateTime inValue) {
        if (canHandleNull || inValue != null) {
            return inValue;
        } else {
            return LocalDateTime.now();
        }
    }

    private static boolean canHandleNullValues(String userAgent) {
        // for invalid or empty user agents we assume a browser or something else,
        // so we don't care if they can handle null
        if (StringUtils.isNullOrEmpty(userAgent)) {
            return true;
        }

        // User agent should look like this: "Aare/1.6.1 CFNetwork/609 Darwin/13.0.0"
        Matcher matcher = USER_AGENT_PATTERN.matcher(userAgent);

        // no match means no app
        if (!matcher.matches()) {
            return true;
        }

        // we know it matches so we can get the first capture group (1 based)
        String ver = matcher.group(1);

        // since hierarchical version numbers should be sortable, we can do a text compare here
        return ver.compareTo(CAN_HANDLE_NULL_VERSION) >= 0;
    }
}
