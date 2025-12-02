package com.example.OLSHEETS.data;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@DiscriminatorValue("INSTRUMENT")
public class Instrument extends Item {

    private Integer age;
    private String type;

    @Enumerated(EnumType.STRING)
    private InstrumentFamily family;

    public Instrument() {
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public InstrumentFamily getFamily() {
        return family;
    }

    public void setFamily(InstrumentFamily family) {
        this.family = family;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Instrument)) return false;
        if (!super.equals(o)) return false;

        Instrument that = (Instrument) o;

        if (age != null ? !age.equals(that.age) : that.age != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return family == that.family;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (age != null ? age.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (family != null ? family.hashCode() : 0);
        return result;
    }
}