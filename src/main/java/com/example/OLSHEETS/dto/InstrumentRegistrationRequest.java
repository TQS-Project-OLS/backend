package com.example.OLSHEETS.dto;

import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.InstrumentType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentRegistrationRequest {
    
    private String name;
    private String description;
    private Double price;
    private Integer ownerId;
    private Integer age;
    private InstrumentType type;
    private InstrumentFamily family;
    private List<String> photoPaths;
}
