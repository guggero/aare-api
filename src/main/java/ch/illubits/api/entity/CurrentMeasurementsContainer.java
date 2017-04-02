package ch.illubits.api.entity;

import java.time.LocalDateTime;

public class CurrentMeasurementsContainer {

    private String name;
    private String number;
    private LocalDateTime date;
    private Float temperature;
    private Float waterlevel;
    private Float discharge;
    private Float temperature24hAgo;
    private Float waterlevel24hAgo;
    private Float discharge24hAgo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Float getWaterlevel() {
        return waterlevel;
    }

    public void setWaterlevel(Float waterlevel) {
        this.waterlevel = waterlevel;
    }

    public Float getDischarge() {
        return discharge;
    }

    public void setDischarge(Float discharge) {
        this.discharge = discharge;
    }

    public Float getTemperature24hAgo() {
        return temperature24hAgo;
    }

    public void setTemperature24hAgo(Float temperature24hAgo) {
        this.temperature24hAgo = temperature24hAgo;
    }

    public Float getWaterlevel24hAgo() {
        return waterlevel24hAgo;
    }

    public void setWaterlevel24hAgo(Float waterlevel24hAgo) {
        this.waterlevel24hAgo = waterlevel24hAgo;
    }

    public Float getDischarge24hAgo() {
        return discharge24hAgo;
    }

    public void setDischarge24hAgo(Float discharge24hAgo) {
        this.discharge24hAgo = discharge24hAgo;
    }
}
