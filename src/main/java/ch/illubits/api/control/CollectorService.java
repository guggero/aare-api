package ch.illubits.api.control;

import ch.illubits.api.entity.Measurement;
import ch.illubits.api.entity.Station;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

@Singleton
@Startup
public class CollectorService {

    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectorService.class);

    private static final String STORE_LAST_UPDATE = "UPDATE Config SET value = NOW() WHERE id = 'measurement.last.update'";
    private static final String MOVE_OLD_RECORDS = "INSERT INTO MeasurementHistory SELECT * FROM Measurement WHERE measure_time < :yesterday";
    private static final String DELETE_OLD_RECORDS = "DELETE FROM Measurement WHERE measure_time < :yesterday";

    private static final String XML_URL = "http://www.hydrodaten.admin.ch/lhg/SMS.xml";

    @Inject
    private ConfigRepository configRepository;

    @Inject
    private StationRepository stationRepository;

    @Inject
    private UnitRepository unitRepository;

    @Inject
    private MeasurementRepository measurementRepository;

    @PostConstruct
    @Schedule(hour = "*", minute = "1,11,21,31,41,51")
    public void collect() {
        String stationList = configRepository.find("station.include.list").getValue();
        List<Station> stations = stationRepository.getStations(asList(stationList.split(",")));

        LOGGER.info("Loaded {} stations from the database", stations.size());

        List<Measurement> measurements = new ArrayList<>();

        try {
            parseXML(stations, measurements);
        } catch (Exception e) {
            throw new EJBException(e);
        }

        LOGGER.info("Loaded {} new measurements", measurements.size());
        measurements.forEach(m -> {
            Measurement existing = measurementRepository.findByStationAndUnitAndMeasureTime(m.getStation().getId(), m.getUnit().getId(), m.getMeasureTime());
            if (existing == null) {
                measurementRepository.persist(m);
                measurementRepository.flush();
            }
        });

        LOGGER.info("Storing last update to database");
        configRepository.executeNativeQuery(STORE_LAST_UPDATE);

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LOGGER.info("Moving records older than {} to history table", yesterday);
        int moved = measurementRepository.executeNativeQuery(MOVE_OLD_RECORDS, new HashMap<String, Object>() {{
            put("yesterday", yesterday);
        }});
        int deleted = measurementRepository.executeNativeQuery(DELETE_OLD_RECORDS, new HashMap<String, Object>() {{
            put("yesterday", yesterday);
        }});

        LOGGER.info("All done. Moved {} records to history table and deleted {} records.", moved, deleted);
    }

    private void parseXML(List<Station> stations, List<Measurement> measurements) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(XML_URL);
        document.getDocumentElement().normalize();
        NodeList nodes = document.getDocumentElement().getElementsByTagName("MesPar");

        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Station station = getStationByNumber(stations, attributes.getNamedItem("StrNr").getNodeValue());
            if (station != null) {
                Measurement measurement = new Measurement();
                measurement.setStation(station);

                long stationId = Long.valueOf(attributes.getNamedItem("Typ").getNodeValue(), 10);
                measurement.setUnit(unitRepository.getReference(stationId));
                measurement.setMeasureTime(reformatDate(getNodeValue(node, "Datum") + " " + getNodeValue(node, "Zeit")));
                measurement.setValue(getNodeValueFloat(node));
                measurement.setValue24h(getNodeValueFloat(node, "dt", "-24h"));
                measurement.setDelta24h(getNodeValueFloat(node, "Typ", "delta24"));
                measurement.setMean24h(getNodeValueFloat(node, "Typ", "m24"));
                measurement.setMax24h(getNodeValueFloat(node, "Typ", "max24"));
                measurement.setMin24h(getNodeValueFloat(node, "Typ", "min24"));
                measurements.add(measurement);
            }
        }
    }

    private static String getNodeValue(Node node, String name) {
        Element e = (Element) node;
        Node n = e.getElementsByTagName(name).item(0).getChildNodes().item(0);
        return n.getNodeValue();
    }

    private static String getNodeValue(Node node, String attributeName, String attributeValue) {
        Element e = (Element) node;
        NodeList nodes = e.getElementsByTagName("Wert");

        for (int i = 0; i < nodes.getLength(); ++i) {
            NamedNodeMap attributes = nodes.item(i).getAttributes();
            if (attributes.getNamedItem(attributeName).getNodeValue().equals(attributeValue)) {
                Node n = nodes.item(i).getChildNodes().item(0);
                if (n != null) {
                    return n.getNodeValue();
                }

                return "NULL";
            }
        }

        return null;
    }

    private static Float getNodeValueFloat(Node node) {
        String value = getNodeValue(node, "Wert");
        if (value == null || value.equalsIgnoreCase("null")) {
            return null;
        }
        return Float.valueOf(value);
    }

    private static Float getNodeValueFloat(Node node, String attributeName, String attributeValue) {
        String value = getNodeValue(node, attributeName, attributeValue);
        if (value == null || value.equalsIgnoreCase("null")) {
            return null;
        }
        return Float.valueOf(value);
    }

    private static LocalDateTime reformatDate(String inputDate) throws ParseException {
        return LocalDateTime.parse(inputDate, INPUT_DATE_FORMAT);
    }

    private static Station getStationByNumber(List<Station> stations, String number) {
        return stations
                .stream()
                .filter(s -> Objects.equals(s.getNumber(), number))
                .findFirst()
                .orElse(null);
    }
}
