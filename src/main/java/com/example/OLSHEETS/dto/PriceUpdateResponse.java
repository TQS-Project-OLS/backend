package com.example.OLSHEETS.dto;

public class PriceUpdateResponse {
    private Long itemId;
    private String itemName;
    private Double newPrice;

    public PriceUpdateResponse(Long itemId, String itemName, Double newPrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.newPrice = newPrice;
    }

    public Long getItemId() { 
        return itemId; 
    }
    
    public void setItemId(Long itemId) { 
        this.itemId = itemId; 
    }

    public String getItemName() { 
        return itemName; 
    }
    
    public void setItemName(String itemName) { 
        this.itemName = itemName; 
    }

    public Double getNewPrice() { 
        return newPrice; 
    }
    
    public void setNewPrice(Double newPrice) { 
        this.newPrice = newPrice; 
    }
}
