package com.example.OLSHEETS.dto;

import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.InstrumentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentRegistrationRequest {
    
    private String name;
    private String description;
    private Double price;
    private Long ownerId;
    private Integer age;
    private InstrumentType type;
    private InstrumentFamily family;
    private List<String> photoPaths;
}