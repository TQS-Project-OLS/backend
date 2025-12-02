package com.example.OLSHEETS.dto;

public class PriceUpdateRequest {
    private Double newPrice;

    public PriceUpdateRequest() {
        // Default constructor for JSON deserialization
    }

    public Double getNewPrice() { 
        return newPrice; 
    }
    
    public void setNewPrice(Double newPrice) { 
        this.newPrice = newPrice; 
    }
}
