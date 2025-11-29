package com.example.OLSHEETS.entity;

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
public class InstrumentEntity extends ItemEntity {

    private Integer age;

    private String type;

    private String family;
}
