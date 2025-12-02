package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTest {

    private MusicSheet musicSheet1;
    private MusicSheet musicSheet2;
    private Instrument instrument1;
    private Instrument instrument2;

    @BeforeEach
    void setUp() {
        musicSheet1 = new MusicSheet();
        musicSheet1.setId(1L);
        musicSheet1.setName("Test Sheet");
        musicSheet1.setDescription("A test music sheet");
        musicSheet1.setOwnerId(100);
        musicSheet1.setPrice(10.0);
        musicSheet1.setComposer("Composer 1");

        musicSheet2 = new MusicSheet();
        musicSheet2.setId(1L);
        musicSheet2.setName("Test Sheet");
        musicSheet2.setDescription("A test music sheet");
        musicSheet2.setOwnerId(100);
        musicSheet2.setPrice(10.0);
        musicSheet2.setComposer("Composer 1");

        instrument1 = new Instrument();
        instrument1.setId(2L);
        instrument1.setName("Test Instrument");
        instrument1.setDescription("A test instrument");
        instrument1.setOwnerId(200);
        instrument1.setPrice(500.0);
        instrument1.setAge(5);
        instrument1.setType(InstrumentType.ACOUSTIC);
        instrument1.setFamily(InstrumentFamily.STRING);

        instrument2 = new Instrument();
        instrument2.setId(2L);
        instrument2.setName("Test Instrument");
        instrument2.setDescription("A test instrument");
        instrument2.setOwnerId(200);
        instrument2.setPrice(500.0);
        instrument2.setAge(5);
        instrument2.setType(InstrumentType.ACOUSTIC);
        instrument2.setFamily(InstrumentFamily.STRING);
    }

    @Test
    void testGettersAndSetters() {
        Item item = new MusicSheet();
        
        item.setId(10L);
        assertThat(item.getId()).isEqualTo(10L);
        
        item.setName("Item Name");
        assertThat(item.getName()).isEqualTo("Item Name");
        
        item.setDescription("Item Description");
        assertThat(item.getDescription()).isEqualTo("Item Description");
        
        item.setOwnerId(99);
        assertThat(item.getOwnerId()).isEqualTo(99);
        
        item.setPrice(99.99);
        assertThat(item.getPrice()).isEqualTo(99.99);
    }

    @Test
    void testEqualsWithSameObject() {
        assertThat(musicSheet1.equals(musicSheet1)).isTrue();
        assertThat(instrument1.equals(instrument1)).isTrue();
    }

    @Test
    void testEqualsWithEqualObjects() {
        assertThat(musicSheet1.equals(musicSheet2)).isTrue();
        assertThat(musicSheet2.equals(musicSheet1)).isTrue();
        
        assertThat(instrument1.equals(instrument2)).isTrue();
        assertThat(instrument2.equals(instrument1)).isTrue();
    }

    @Test
    void testEqualsWithDifferentIds() {
        musicSheet2.setId(2L);
        assertThat(musicSheet1.equals(musicSheet2)).isFalse();
    }

    @Test
    void testEqualsWithDifferentNames() {
        musicSheet2.setName("Different Name");
        assertThat(musicSheet1.equals(musicSheet2)).isFalse();
    }

    @Test
    void testEqualsWithDifferentDescriptions() {
        musicSheet2.setDescription("Different Description");
        assertThat(musicSheet1.equals(musicSheet2)).isFalse();
    }

    @Test
    void testEqualsWithDifferentOwnerIds() {
        musicSheet2.setOwnerId(999);
        assertThat(musicSheet1.equals(musicSheet2)).isFalse();
    }

    @Test
    void testEqualsWithDifferentPrices() {
        musicSheet2.setPrice(999.99);
        assertThat(musicSheet1.equals(musicSheet2)).isFalse();
    }

    @Test
    void testEqualsWithNull() {
        assertThat(musicSheet1.equals(null)).isFalse();
        assertThat(instrument1.equals(null)).isFalse();
    }

    @Test
    void testEqualsWithDifferentClass() {
        assertThat(musicSheet1.equals(instrument1)).isFalse();
        assertThat(musicSheet1.equals("String Object")).isFalse();
    }

    @Test
    void testEqualsWithNullFields() {
        MusicSheet sheet1 = new MusicSheet();
        MusicSheet sheet2 = new MusicSheet();
        
        assertThat(sheet1.equals(sheet2)).isTrue();
        
        sheet1.setId(1L);
        assertThat(sheet1.equals(sheet2)).isFalse();
        
        sheet2.setId(1L);
        assertThat(sheet1.equals(sheet2)).isTrue();
        
        sheet1.setName("Name");
        assertThat(sheet1.equals(sheet2)).isFalse();
        
        sheet2.setName("Name");
        assertThat(sheet1.equals(sheet2)).isTrue();
    }

    @Test
    void testHashCodeConsistency() {
        assertThat(musicSheet1.hashCode()).isEqualTo(musicSheet2.hashCode());
        assertThat(instrument1.hashCode()).isEqualTo(instrument2.hashCode());
    }

    @Test
    void testHashCodeWithDifferentValues() {
        musicSheet2.setName("Different Name");
        assertThat(musicSheet1.hashCode()).isNotEqualTo(musicSheet2.hashCode());
    }

    @Test
    void testHashCodeWithNullFields() {
        MusicSheet sheet = new MusicSheet();
        int hashCode = sheet.hashCode();
        assertThat(hashCode).isNotNull();
    }

    @Test
    void testEqualsWithNullIdBothObjects() {
        MusicSheet sheet1 = new MusicSheet();
        sheet1.setName("Name");
        sheet1.setOwnerId(1);
        sheet1.setPrice(10.0);
        
        MusicSheet sheet2 = new MusicSheet();
        sheet2.setName("Name");
        sheet2.setOwnerId(1);
        sheet2.setPrice(10.0);
        
        assertThat(sheet1.equals(sheet2)).isTrue();
    }

    @Test
    void testEqualsWithNullNameBothObjects() {
        MusicSheet sheet1 = new MusicSheet();
        sheet1.setId(1L);
        sheet1.setOwnerId(1);
        sheet1.setPrice(10.0);
        
        MusicSheet sheet2 = new MusicSheet();
        sheet2.setId(1L);
        sheet2.setOwnerId(1);
        sheet2.setPrice(10.0);
        
        assertThat(sheet1.equals(sheet2)).isTrue();
    }

    @Test
    void testEqualsWithNullDescriptionBothObjects() {
        MusicSheet sheet1 = new MusicSheet();
        sheet1.setId(1L);
        sheet1.setName("Name");
        sheet1.setOwnerId(1);
        sheet1.setPrice(10.0);
        
        MusicSheet sheet2 = new MusicSheet();
        sheet2.setId(1L);
        sheet2.setName("Name");
        sheet2.setOwnerId(1);
        sheet2.setPrice(10.0);
        
        assertThat(sheet1.equals(sheet2)).isTrue();
    }

    @Test
    void testEqualsWithNullPriceBothObjects() {
        MusicSheet sheet1 = new MusicSheet();
        sheet1.setId(1L);
        sheet1.setName("Name");
        sheet1.setOwnerId(1);
        
        MusicSheet sheet2 = new MusicSheet();
        sheet2.setId(1L);
        sheet2.setName("Name");
        sheet2.setOwnerId(1);
        
        assertThat(sheet1.equals(sheet2)).isTrue();
    }

    @Test
    void testEqualsWithOneNullIdAndOneNonNull() {
        MusicSheet sheet1 = new MusicSheet();
        sheet1.setName("Name");
        
        MusicSheet sheet2 = new MusicSheet();
        sheet2.setId(1L);
        sheet2.setName("Name");
        
        assertThat(sheet1.equals(sheet2)).isFalse();
    }

    @Test
    void testEqualsWithOneNullNameAndOneNonNull() {
        MusicSheet sheet1 = new MusicSheet();
        sheet1.setId(1L);
        
        MusicSheet sheet2 = new MusicSheet();
        sheet2.setId(1L);
        sheet2.setName("Name");
        
        assertThat(sheet1.equals(sheet2)).isFalse();
    }

    @Test
    void testEqualsWithOneNullDescriptionAndOneNonNull() {
        MusicSheet sheet1 = new MusicSheet();
        sheet1.setId(1L);
        sheet1.setName("Name");
        
        MusicSheet sheet2 = new MusicSheet();
        sheet2.setId(1L);
        sheet2.setName("Name");
        sheet2.setDescription("Description");
        
        assertThat(sheet1.equals(sheet2)).isFalse();
    }

    @Test
    void testEqualsWithOneNullPriceAndOneNonNull() {
        MusicSheet sheet1 = new MusicSheet();
        sheet1.setId(1L);
        sheet1.setName("Name");
        
        MusicSheet sheet2 = new MusicSheet();
        sheet2.setId(1L);
        sheet2.setName("Name");
        sheet2.setPrice(10.0);
        
        assertThat(sheet1.equals(sheet2)).isFalse();
    }

    @Test
    void testEqualsWithDifferentSubclassTypes() {
        MusicSheet sheet = new MusicSheet();
        sheet.setId(1L);
        sheet.setName("Test");
        sheet.setOwnerId(1);
        sheet.setPrice(10.0);
        
        Instrument instrument = new Instrument();
        instrument.setId(1L);
        instrument.setName("Test");
        instrument.setOwnerId(1);
        instrument.setPrice(10.0);
        
        assertThat(sheet.equals(instrument)).isFalse();
    }

    @Test
    void testItemConstructor() {
        MusicSheet sheet = new MusicSheet();
        assertThat(sheet).isNotNull();
    }

    @Test
    void testPriceNullInFirstObjectButNotInSecond() {
        Instrument instr1 = new Instrument();
        instr1.setId(1L);
        instr1.setName("Name");
        instr1.setOwnerId(1);
        instr1.setPrice(null);
        
        Instrument instr2 = new Instrument();
        instr2.setId(1L);
        instr2.setName("Name");
        instr2.setOwnerId(1);
        instr2.setPrice(10.0);
        
        assertThat(instr1.equals(instr2)).isFalse();
    }

    @Test
    void testDescriptionNullInSecondObjectButNotInFirst() {
        Instrument instr1 = new Instrument();
        instr1.setId(1L);
        instr1.setName("Name");
        instr1.setOwnerId(1);
        instr1.setDescription("Description");
        
        Instrument instr2 = new Instrument();
        instr2.setId(1L);
        instr2.setName("Name");
        instr2.setOwnerId(1);
        instr2.setDescription(null);
        
        assertThat(instr1.equals(instr2)).isFalse();
    }

    @Test
    void testIdNullInSecondObjectButNotInFirst() {
        Instrument instr1 = new Instrument();
        instr1.setId(1L);
        instr1.setName("Name");
        
        Instrument instr2 = new Instrument();
        instr2.setId(null);
        instr2.setName("Name");
        
        assertThat(instr1.equals(instr2)).isFalse();
    }

    @Test
    void testNameNullInSecondObjectButNotInFirst() {
        Instrument instr1 = new Instrument();
        instr1.setId(1L);
        instr1.setName("Name");
        
        Instrument instr2 = new Instrument();
        instr2.setId(1L);
        instr2.setName(null);
        
        assertThat(instr1.equals(instr2)).isFalse();
    }
}