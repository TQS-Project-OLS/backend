package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.MusicSheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InstrumentTest {

    private Instrument instrument1;
    private Instrument instrument2;

    @BeforeEach
    void setUp() {
        instrument1 = new Instrument();
        instrument1.setId(1L);
        instrument1.setName("Guitar");
        instrument1.setDescription("Acoustic guitar");
        instrument1.setOwnerId(100);
        instrument1.setPrice(500.0);
        instrument1.setAge(5);
        instrument1.setType(InstrumentType.ACOUSTIC);
        instrument1.setFamily(InstrumentFamily.STRING);

        instrument2 = new Instrument();
        instrument2.setId(1L);
        instrument2.setName("Guitar");
        instrument2.setDescription("Acoustic guitar");
        instrument2.setOwnerId(100);
        instrument2.setPrice(500.0);
        instrument2.setAge(5);
        instrument2.setType(InstrumentType.ACOUSTIC);
        instrument2.setFamily(InstrumentFamily.STRING);
    }

    @Test
    void testDefaultConstructor() {
        Instrument instrument = new Instrument();
        assertThat(instrument).isNotNull();
        assertThat(instrument.getAge()).isNull();
        assertThat(instrument.getType()).isNull();
        assertThat(instrument.getFamily()).isNull();
    }

    @Test
    void testAgeGetterAndSetter() {
        Instrument instrument = new Instrument();
        instrument.setAge(10);
        assertThat(instrument.getAge()).isEqualTo(10);
    }

    @Test
    void testTypeGetterAndSetter() {
        Instrument instrument = new Instrument();
        instrument.setType(InstrumentType.ELECTRIC);
        assertThat(instrument.getType()).isEqualTo(InstrumentType.ELECTRIC);
    }

    @Test
    void testFamilyGetterAndSetter() {
        Instrument instrument = new Instrument();
        instrument.setFamily(InstrumentFamily.PERCUSSION);
        assertThat(instrument.getFamily()).isEqualTo(InstrumentFamily.PERCUSSION);
    }

    @Test
    void testEqualsWithSameObject() {
        assertThat(instrument1.equals(instrument1)).isTrue();
    }

    @Test
    void testEqualsWithEqualObjects() {
        assertThat(instrument1.equals(instrument2)).isTrue();
        assertThat(instrument2.equals(instrument1)).isTrue();
    }

    @Test
    void testEqualsWithNull() {
        assertThat(instrument1.equals(null)).isFalse();
    }

    @Test
    void testEqualsWithDifferentClass() {
        assertThat(instrument1.equals("String")).isFalse();
        assertThat(instrument1.equals(new Object())).isFalse();
        
        MusicSheet sheet = new MusicSheet();
        sheet.setId(1L);
        sheet.setName("Guitar");
        assertThat(instrument1.equals(sheet)).isFalse();
    }

    @Test
    void testEqualsWithDifferentAge() {
        instrument2.setAge(10);
        assertThat(instrument1.equals(instrument2)).isFalse();
    }

    @Test
    void testEqualsWithDifferentType() {
        instrument2.setType(InstrumentType.ELECTRIC);
        assertThat(instrument1.equals(instrument2)).isFalse();
    }

    @Test
    void testEqualsWithDifferentFamily() {
        instrument2.setFamily(InstrumentFamily.BRASS);
        assertThat(instrument1.equals(instrument2)).isFalse();
    }

    @Test
    void testEqualsWithNullFields() {
        Instrument instr1 = new Instrument();
        instr1.setName("Piano");
        instr1.setType(InstrumentType.ACOUSTIC);
        instr1.setFamily(InstrumentFamily.KEYBOARD);
        
        Instrument instr2 = new Instrument();
        instr2.setName("Piano");
        instr2.setType(InstrumentType.ACOUSTIC);
        instr2.setFamily(InstrumentFamily.KEYBOARD);
        
        assertThat(instr1.equals(instr2)).isTrue();
        
        // Test one null vs non-null
        instrument2.setAge(null);
        assertThat(instrument1.equals(instrument2)).isFalse();
        
        instrument1.setType(null);
        assertThat(instrument1.equals(instrument2)).isFalse();
    }

    @Test
    void testEqualsCallsSuperEquals() {
        instrument2.setName("Different Name");
        assertThat(instrument1.equals(instrument2)).isFalse();
    }

    @Test
    void testHashCodeConsistency() {
        assertThat(instrument1).hasSameHashCodeAs(instrument2);
        
        instrument2.setAge(10);
        assertThat(instrument1.hashCode()).isNotEqualTo(instrument2.hashCode());
        
        Instrument instrument = new Instrument();
        assertThat(instrument).hasSameHashCodeAs(new Instrument());
    }

    @Test
    void testInstrumentTypesAndFamilies() {
        instrument1.setType(InstrumentType.ELECTRIC);
        assertThat(instrument1.getType()).isEqualTo(InstrumentType.ELECTRIC);
        
        instrument1.setFamily(InstrumentFamily.BRASS);
        assertThat(instrument1.getFamily()).isEqualTo(InstrumentFamily.BRASS);
    }

    @Test
    void testInstrumentInheritance() {
        instrument1.setName("Test Instrument");
        instrument1.setDescription("Test Description");
        instrument1.setOwnerId(999);
        instrument1.setPrice(1000.0);
        
        assertThat(instrument1.getName()).isEqualTo("Test Instrument");
        assertThat(instrument1.getDescription()).isEqualTo("Test Description");
        assertThat(instrument1.getOwnerId()).isEqualTo(999);
        assertThat(instrument1.getPrice()).isEqualTo(1000.0);
    }
}