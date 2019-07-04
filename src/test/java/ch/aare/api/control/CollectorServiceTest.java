package ch.aare.api.control;

import ch.aare.api.entity.Config;
import ch.aare.api.entity.Station;
import ch.aare.api.entity.Unit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CollectorServiceTest {

    @InjectMocks
    private CollectorService service;

    @Mock
    private ConfigRepository configRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private MeasurementRepository measurementRepository;

    private Station bern;

    @Before
    public void init() {
        Config config = new Config();
        config.setValue("2135");
        doReturn(Optional.of(config)).when(configRepository).findById("station.include.list");

        bern = new Station();
        bern.setNumber("2135");
        bern.setId(123L);
        bern.setName("Aare - Bern");
        doReturn(singletonList(bern)).when(stationRepository).findAllByNumberIn(any());

        Unit unit = new Unit();
        unit.setId(345L);
        doReturn(unit).when(unitRepository).getOne(anyLong());
        doReturn(Optional.empty()).when(measurementRepository).findByStation_IdAndUnit_IdAndMeasureTime(anyLong(), anyLong(), any());
    }

    @Test
    public void shouldCollectData() {
        // when
        service.collect();

        // then
        verify(measurementRepository, atLeastOnce()).findByStation_IdAndUnit_IdAndMeasureTime(eq(123L), eq(345L), any());
    }
}
