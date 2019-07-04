package ch.aare.api.control;

import ch.aare.api.entity.Config;
import ch.aare.api.entity.Unit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;

@Service
@Transactional
public class UnitService {

    public static final String UNIT_WATER_LEVEL = "unit.waterlevel";
    public static final String UNIT_TEMPERATURE = "unit.temperature";
    public static final String UNIT_RUNOFF = "unit.runoff";

    public static final List<String> CACHED_UNITS = asList(UNIT_WATER_LEVEL, UNIT_TEMPERATURE, UNIT_RUNOFF);

    private static final Map<String, Unit> UNIT_CACHE = new HashMap<>();

    private final ConfigRepository configRepository;
    private final UnitRepository unitRepository;

    public UnitService(ConfigRepository configRepository, UnitRepository unitRepository) {
        this.configRepository = configRepository;
        this.unitRepository = unitRepository;
    }

    @PostConstruct
    @Scheduled(cron = "0 */5 * * * *")
    public void initCache() {
        synchronized (UNIT_CACHE) {
            CACHED_UNITS.forEach(unitKey -> {
                Optional<Config> configOptional = configRepository.findById(unitKey);

                if (!configOptional.isPresent()) {
                    throw new RuntimeException("Config not found for key '" + unitKey + "'");
                }

                Long unitId = Long.valueOf(configOptional.get().getValue());
                Optional<Unit> unitOptional = unitRepository.findById(unitId);
                if (!unitOptional.isPresent()) {
                    throw new RuntimeException("Unit not found by ID '" + unitId + "'");
                }
                UNIT_CACHE.put(unitKey, unitOptional.get());
            });
        }
    }

    public Unit getUnit(String key) {
        synchronized (UNIT_CACHE) {
            return UNIT_CACHE.get(key);
        }
    }
}
