package com.example.OLSHEETS.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdateResponse {
    private Long itemId;
    private String itemName;
    private Double newPrice;
}