package com.example.OLSHEETS.data;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class MusicSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String category;
    private String description;
    private BigDecimal pricePerDay;
    private Long ownerId;
    private boolean available;

    public MusicSheet() {}

    public MusicSheet(String title, String category, String description, BigDecimal pricePerDay, Long ownerId) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.pricePerDay = pricePerDay;
        this.ownerId = ownerId;
        this.available = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}

