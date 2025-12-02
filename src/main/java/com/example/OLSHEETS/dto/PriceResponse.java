package com.example.OLSHEETS.dto;

public class PriceResponse {
    private Long itemId;
    private Double price;

    public PriceResponse(Long itemId, Double price) {
        this.itemId = itemId;
        this.price = price;
    }

    public Long getItemId() { 
        return itemId; 
    }
    
    public void setItemId(Long itemId) { 
        this.itemId = itemId; 
    }

    public Double getPrice() { 
        return price; 
    }
    
    public void setPrice(Double price) { 
        this.price = price; 
    }
}
