package ch.aare.api.control;

import ch.aare.api.entity.Config;
import ch.aare.api.entity.Measurement;
import ch.aare.api.entity.Station;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Arrays.asList;

@Service
@Transactional
public class CollectorService {

    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectorService.class);

    private static final String XML_URL = "https://www.hydrodaten.admin.ch/lhg/SMS.xml";

    private final ConfigRepository configRepository;
    private final StationRepository stationRepository;
    private final UnitRepository unitRepository;
    private final MeasurementRepository measurementRepository;

    public CollectorService(ConfigRepository configRepository, StationRepository stationRepository, UnitRepository unitRepository,
                            MeasurementRepository measurementRepository) {
        this.configRepository = configRepository;
        this.stationRepository = stationRepository;
        this.unitRepository = unitRepository;
        this.measurementRepository = measurementRepository;
    }

    @Scheduled(cron = "0 */3 * * * *")
    public void collect() {
        Optional<Config> configOptional = configRepository.findById("station.include.list");
        if (!configOptional.isPresent()) {
            throw new RuntimeException("Could not find station list!");
        }
        String stationList = configOptional.get().getValue();
        List<Station> stations = stationRepository.findAllByNumberIn(asList(stationList.split(",")));

        LOGGER.info("Loaded {} stations from the database", stations.size());

        List<Measurement> measurements = new ArrayList<>();

        try {
            parseXML(stations, measurements);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Loaded {} new measurements", measurements.size());
        measurements.forEach(m -> {
            Optional<Measurement> existing = measurementRepository.findByStation_IdAndUnit_IdAndMeasureTime(m.getStation().getId(), m.getUnit().getId(), m.getMeasureTime());
            if (!existing.isPresent()) {
                measurementRepository.saveAndFlush(m);
                measurementRepository.flush();
            }
        });

        LOGGER.info("Storing last update to database");
        configRepository.updateMeasureTime();

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LOGGER.info("Moving records older than {} to history table", yesterday);
        int moved = measurementRepository.moveOldRecords(yesterday);
        int deleted = measurementRepository.deleteOldRecords(yesterday);

        LOGGER.info("All done. Moved {} records to history table and deleted {} records.", moved, deleted);
    }

    private void parseXML(List<Station> stations, List<Measurement> measurements) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        dbf.setFeature("http://xml.org/sax/features/namespaces", false);
        dbf.setFeature("http://xml.org/sax/features/validation", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

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

                long unitId = Long.valueOf(attributes.getNamedItem("Typ").getNodeValue(), 10);
                measurement.setUnit(unitRepository.getOne(unitId));
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
            if (attributes != null && attributes.getNamedItem(attributeName) != null) {
                Node namedItem = attributes.getNamedItem(attributeName);
                if (namedItem != null && namedItem.getNodeValue().equals(attributeValue)) {
                    Node n = nodes.item(i).getChildNodes().item(0);
                    if (n != null) {
                        return n.getNodeValue();
                    }

                    return "NULL";
                }
            }
        }

        return null;
    }

    private static Float getNodeValueFloat(Node node) {
        String value = getNodeValue(node, "Wert");
        if (value == null || value.equalsIgnoreCase("null")) {
            return null;
        }
        value = value.replaceAll("[' ,]", "");
        return Float.valueOf(value);
    }

    private static Float getNodeValueFloat(Node node, String attributeName, String attributeValue) {
        String value = getNodeValue(node, attributeName, attributeValue);
        if (value == null || value.equalsIgnoreCase("null")) {
            return null;
        }
        value = value.replaceAll("[' ,]", "");
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
