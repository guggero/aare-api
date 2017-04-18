package ch.illubits.api.boundary;

import ch.illubits.api.StringUtils;
import ch.illubits.api.control.MeasurementRepository;
import ch.illubits.api.control.StationRepository;
import ch.illubits.api.control.UnitService;
import ch.illubits.api.entity.CurrentMeasurementsContainer;
import ch.illubits.api.entity.Measurement;
import ch.illubits.api.entity.Station;
import ch.illubits.api.entity.Unit;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.illubits.api.control.UnitService.*;
import static java.util.Comparator.comparing;
import static java.util.regex.Pattern.*;
import static javax.ws.rs.core.HttpHeaders.USER_AGENT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Stateless
@Path("v1")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class V1Resource {

    private static final Pattern USER_AGENT_PATTERN = Pattern.compile("aare/([\\d\\.a-z]+) .*", CASE_INSENSITIVE | DOTALL | MULTILINE);
    private static final String CAN_HANDLE_NULL_VERSION = "1.6.2";

    @Inject
    private MeasurementRepository measurementRepository;

    @Inject
    private StationRepository stationRepository;

    @Inject
    private UnitService unitService;

    @GET
    @Path("/current")
    public Response getCurrentMeasurements(@HeaderParam(USER_AGENT) String userAgent) {
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

        return Response.ok(new TreeMap<String, Object>() {{
            put("date", LocalDateTime.now());
            put("dateformat", "unix");
            put("source", "BAFU");
            put("stations", currentMeasurements);
        }}).build();
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

        return measurementRepository.findByStationAndUnitOrderByMeasureTime(station.getId(), unit.getId());
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
