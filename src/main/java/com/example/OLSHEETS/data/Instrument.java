package com.example.OLSHEETS.data;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
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

    @Enumerated(EnumType.STRING)
    private InstrumentType type;

    @Enumerated(EnumType.STRING)
    private InstrumentFamily family;
}
