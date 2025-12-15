package com.example.OLSHEETS.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MusicSheetRegistrationRequest {
    
    private String name;
    private String description;
    private Double price;
    private Long ownerId;
    private String category;
    private String composer;
    private String instrumentation;
    private Float duration;
    private List<String> photoPaths;
}
