package com.example.OLSHEETS.data;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("INSTRUMENT")
public class Instrument extends Item {

    private Integer age;
    private String type;
    private String family;
}
