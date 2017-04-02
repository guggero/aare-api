package ch.illubits.api.control;

import ch.illubits.api.entity.Config;
import ch.illubits.api.entity.Unit;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@Singleton
@Startup
public class UnitService {

    public static final String UNIT_WATER_LEVEL = "unit.waterlevel";
    public static final String UNIT_TEMPERATURE = "unit.temperature";
    public static final String UNIT_RUNOFF = "unit.runoff";

    public static final List<String> CACHED_UNITS = asList(UNIT_WATER_LEVEL, UNIT_TEMPERATURE, UNIT_RUNOFF);

    private static final Map<String, Unit> UNIT_CACHE = new HashMap<>();

    @Inject
    private ConfigRepository configRepository;

    @Inject
    private UnitRepository unitRepository;

    @PostConstruct
    @Schedule(hour = "*", minute = "*/5")
    public void initCache() {
        synchronized (UNIT_CACHE) {
            CACHED_UNITS.forEach(unitKey -> {
                Config idConfig = configRepository.find(unitKey);

                if (idConfig == null || idConfig.getValue() == null) {
                    throw new RuntimeException("Config not found for key '" + unitKey + "'");
                }

                Long unitId = Long.valueOf(idConfig.getValue());
                UNIT_CACHE.put(unitKey, unitRepository.find(unitId));
            });
        }
    }

    public Unit getUnit(String key) {
        synchronized (UNIT_CACHE) {
            return UNIT_CACHE.get(key);
        }
    }
}
