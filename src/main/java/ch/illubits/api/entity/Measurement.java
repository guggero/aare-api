package ch.illubits.api.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class Measurement {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    @Column(name = "measure_time")
    private LocalDate measureTime;

    @Column(name = "insert_time")
    private LocalDate insertTime;

    @ManyToOne
    @JoinColumn(name = "unit_Id")
    private Unit unit;

    private Float value;

    @Column(name = "value_24h")
    private Float value24h;

    @Column(name = "delta_24h")
    private Float delta24h;

    @Column(name = "mean_24h")
    private Float mean24h;

    @Column(name = "max_24h")
    private Float max24h;

    @Column(name = "min_24h")
    private Float min24h;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public LocalDate getMeasureTime() {
        return measureTime;
    }

    public void setMeasureTime(LocalDate measureTime) {
        this.measureTime = measureTime;
    }

    public LocalDate getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(LocalDate insertTime) {
        this.insertTime = insertTime;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public Float getValue24h() {
        return value24h;
    }

    public void setValue24h(Float value24h) {
        this.value24h = value24h;
    }

    public Float getDelta24h() {
        return delta24h;
    }

    public void setDelta24h(Float delta24h) {
        this.delta24h = delta24h;
    }

    public Float getMean24h() {
        return mean24h;
    }

    public void setMean24h(Float mean24h) {
        this.mean24h = mean24h;
    }

    public Float getMax24h() {
        return max24h;
    }

    public void setMax24h(Float max24h) {
        this.max24h = max24h;
    }

    public Float getMin24h() {
        return min24h;
    }

    public void setMin24h(Float min24h) {
        this.min24h = min24h;
    }
}
