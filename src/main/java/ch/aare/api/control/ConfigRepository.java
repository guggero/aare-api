package ch.aare.api.control;

import ch.aare.api.entity.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends JpaRepository<Config, String> {

    @Modifying
    @Query(value = "UPDATE config SET value = NOW() WHERE id = 'measurement.last.update'", nativeQuery = true)
    void updateMeasureTime();
}
