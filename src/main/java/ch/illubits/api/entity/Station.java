package ch.illubits.api.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Station {

    @Id
    private Long id;

    private String number;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
