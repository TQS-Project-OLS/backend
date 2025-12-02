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
    void testEqualsWithNullAgeBothObjects() {
        Instrument instr1 = new Instrument();
        instr1.setName("Piano");
        instr1.setType(InstrumentType.ACOUSTIC);
        instr1.setFamily(InstrumentFamily.KEYBOARD);
        
        Instrument instr2 = new Instrument();
        instr2.setName("Piano");
        instr2.setType(InstrumentType.ACOUSTIC);
        instr2.setFamily(InstrumentFamily.KEYBOARD);
        
        assertThat(instr1.equals(instr2)).isTrue();
    }

    @Test
    void testEqualsWithNullTypeBothObjects() {
        Instrument instr1 = new Instrument();
        instr1.setName("Piano");
        instr1.setAge(5);
        instr1.setFamily(InstrumentFamily.KEYBOARD);
        
        Instrument instr2 = new Instrument();
        instr2.setName("Piano");
        instr2.setAge(5);
        instr2.setFamily(InstrumentFamily.KEYBOARD);
        
        assertThat(instr1.equals(instr2)).isTrue();
    }

    @Test
    void testEqualsWithNullFamilyBothObjects() {
        Instrument instr1 = new Instrument();
        instr1.setName("Piano");
        instr1.setAge(5);
        instr1.setType(InstrumentType.ACOUSTIC);
        
        Instrument instr2 = new Instrument();
        instr2.setName("Piano");
        instr2.setAge(5);
        instr2.setType(InstrumentType.ACOUSTIC);
        
        assertThat(instr1.equals(instr2)).isTrue();
    }

    @Test
    void testEqualsWithOneNullAge() {
        instrument2.setAge(null);
        assertThat(instrument1.equals(instrument2)).isFalse();
    }

    @Test
    void testEqualsWithOneNullType() {
        instrument1.setType(null);
        assertThat(instrument1.equals(instrument2)).isFalse();
    }

    @Test
    void testEqualsWithOneNullFamily() {
        instrument2.setFamily(null);
        assertThat(instrument1.equals(instrument2)).isFalse();
    }

    @Test
    void testEqualsCallsSuperEquals() {
        instrument2.setName("Different Name");
        assertThat(instrument1.equals(instrument2)).isFalse();
    }

    @Test
    void testHashCodeConsistency() {
        int hash1 = instrument1.hashCode();
        int hash2 = instrument1.hashCode();
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void testHashCodeEqualObjects() {
        assertThat(instrument1.hashCode()).isEqualTo(instrument2.hashCode());
    }

    @Test
    void testHashCodeWithDifferentAge() {
        instrument2.setAge(10);
        assertThat(instrument1.hashCode()).isNotEqualTo(instrument2.hashCode());
    }

    @Test
    void testHashCodeWithDifferentType() {
        instrument2.setType(InstrumentType.ELECTRIC);
        assertThat(instrument1.hashCode()).isNotEqualTo(instrument2.hashCode());
    }

    @Test
    void testHashCodeWithDifferentFamily() {
        instrument2.setFamily(InstrumentFamily.BRASS);
        assertThat(instrument1.hashCode()).isNotEqualTo(instrument2.hashCode());
    }

    @Test
    void testHashCodeWithNullFields() {
        Instrument instrument = new Instrument();
        int hashCode = instrument.hashCode();
        assertThat(hashCode).isNotNull();
    }

    @Test
    void testHashCodeIncludesAllFields() {
        Instrument instr1 = new Instrument();
        instr1.setName("Violin");
        instr1.setAge(3);
        instr1.setType(InstrumentType.CLASSICAL);
        instr1.setFamily(InstrumentFamily.STRING);
        
        Instrument instr2 = new Instrument();
        instr2.setName("Violin");
        instr2.setAge(3);
        instr2.setType(InstrumentType.CLASSICAL);
        instr2.setFamily(InstrumentFamily.STRING);
        
        assertThat(instr1.hashCode()).isEqualTo(instr2.hashCode());
    }

    @Test
    void testSetAgeWithNull() {
        instrument1.setAge(null);
        assertThat(instrument1.getAge()).isNull();
    }

    @Test
    void testSetTypeWithNull() {
        instrument1.setType(null);
        assertThat(instrument1.getType()).isNull();
    }

    @Test
    void testSetFamilyWithNull() {
        instrument1.setFamily(null);
        assertThat(instrument1.getFamily()).isNull();
    }

    @Test
    void testAllInstrumentTypes() {
        instrument1.setType(InstrumentType.ACOUSTIC);
        assertThat(instrument1.getType()).isEqualTo(InstrumentType.ACOUSTIC);
        
        instrument1.setType(InstrumentType.ELECTRIC);
        assertThat(instrument1.getType()).isEqualTo(InstrumentType.ELECTRIC);
        
        instrument1.setType(InstrumentType.DIGITAL);
        assertThat(instrument1.getType()).isEqualTo(InstrumentType.DIGITAL);
        
        instrument1.setType(InstrumentType.CLASSICAL);
        assertThat(instrument1.getType()).isEqualTo(InstrumentType.CLASSICAL);
        
        instrument1.setType(InstrumentType.BASS);
        assertThat(instrument1.getType()).isEqualTo(InstrumentType.BASS);
        
        instrument1.setType(InstrumentType.DRUMS);
        assertThat(instrument1.getType()).isEqualTo(InstrumentType.DRUMS);
        
        instrument1.setType(InstrumentType.SYNTHESIZER);
        assertThat(instrument1.getType()).isEqualTo(InstrumentType.SYNTHESIZER);
        
        instrument1.setType(InstrumentType.WIND);
        assertThat(instrument1.getType()).isEqualTo(InstrumentType.WIND);
        
        instrument1.setType(InstrumentType.BRASS_INSTRUMENT);
        assertThat(instrument1.getType()).isEqualTo(InstrumentType.BRASS_INSTRUMENT);
        
        instrument1.setType(InstrumentType.PERCUSSION_INSTRUMENT);
        assertThat(instrument1.getType()).isEqualTo(InstrumentType.PERCUSSION_INSTRUMENT);
    }

    @Test
    void testAllInstrumentFamilies() {
        instrument1.setFamily(InstrumentFamily.KEYBOARD);
        assertThat(instrument1.getFamily()).isEqualTo(InstrumentFamily.KEYBOARD);
        
        instrument1.setFamily(InstrumentFamily.STRING);
        assertThat(instrument1.getFamily()).isEqualTo(InstrumentFamily.STRING);
        
        instrument1.setFamily(InstrumentFamily.WOODWIND);
        assertThat(instrument1.getFamily()).isEqualTo(InstrumentFamily.WOODWIND);
        
        instrument1.setFamily(InstrumentFamily.BRASS);
        assertThat(instrument1.getFamily()).isEqualTo(InstrumentFamily.BRASS);
        
        instrument1.setFamily(InstrumentFamily.PERCUSSION);
        assertThat(instrument1.getFamily()).isEqualTo(InstrumentFamily.PERCUSSION);
        
        instrument1.setFamily(InstrumentFamily.GUITAR);
        assertThat(instrument1.getFamily()).isEqualTo(InstrumentFamily.GUITAR);
        
        instrument1.setFamily(InstrumentFamily.ELECTRONIC);
        assertThat(instrument1.getFamily()).isEqualTo(InstrumentFamily.ELECTRONIC);
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

    @Test
    void testSetAgeWithZero() {
        instrument1.setAge(0);
        assertThat(instrument1.getAge()).isEqualTo(0);
    }

    @Test
    void testSetAgeWithNegativeValue() {
        instrument1.setAge(-5);
        assertThat(instrument1.getAge()).isEqualTo(-5);
    }

    @Test
    void testSetAgeWithLargeValue() {
        instrument1.setAge(100);
        assertThat(instrument1.getAge()).isEqualTo(100);
    }
}