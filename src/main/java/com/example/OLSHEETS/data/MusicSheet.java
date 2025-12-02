package com.example.OLSHEETS.data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("MUSIC_SHEET")
public class MusicSheet extends Item {

    private String composer;

    @Enumerated(EnumType.STRING)
    private SheetCategory category;
}
