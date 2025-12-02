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
@DiscriminatorValue("MUSIC_SHEET")
public class MusicSheet extends Item {

    @Enumerated(EnumType.STRING)
    private SheetCategory category;

    private String composer;
}
