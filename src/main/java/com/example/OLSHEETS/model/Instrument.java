package com.example.OLSHEETS.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Instrument extends Item {

    private Integer age;
    private String type;
    private String family;
}
