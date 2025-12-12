package com.example.OLSHEETS.dto;

import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.InstrumentType;

import java.util.List;

public class InstrumentRegistrationRequest {
    
    private String name;
    private String description;
    private Double price;
    private Long ownerId;
    private Integer age;
    private InstrumentType type;
    private InstrumentFamily family;
    private List<String> photoPaths;

    public InstrumentRegistrationRequest() {
    }

    public InstrumentRegistrationRequest(String name, String description, Double price, Long ownerId,
                                         Integer age, InstrumentType type, InstrumentFamily family,
                                         List<String> photoPaths) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.ownerId = ownerId;
        this.age = age;
        this.type = type;
        this.family = family;
        this.photoPaths = photoPaths;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public InstrumentType getType() {
        return type;
    }

    public void setType(InstrumentType type) {
        this.type = type;
    }

    public InstrumentFamily getFamily() {
        return family;
    }

    public void setFamily(InstrumentFamily family) {
        this.family = family;
    }

    public List<String> getPhotoPaths() {
        return photoPaths;
    }

    public void setPhotoPaths(List<String> photoPaths) {
        this.photoPaths = photoPaths;
    }
}