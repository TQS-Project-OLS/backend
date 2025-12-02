package com.example.OLSHEETS.dto;

public class PriceUpdateRequest {
    private Double newPrice;

    public PriceUpdateRequest() {}

    public Double getNewPrice() { 
        return newPrice; 
    }
    
    public void setNewPrice(Double newPrice) { 
        this.newPrice = newPrice; 
    }
}
