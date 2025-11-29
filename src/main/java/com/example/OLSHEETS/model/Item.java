package com.example.OLSHEETS.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class Item {

    private Long id;
    private String name;
    private String description;
    private int ownerId;
    private Double price;

    // TODO: can a service to get images if the item
    // public List<String> images();
}
