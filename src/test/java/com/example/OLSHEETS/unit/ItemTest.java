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
        com.example.OLSHEETS.data.User owner100 = new com.example.OLSHEETS.data.User("owner100");
        owner100.setId(100L);
        musicSheet1.setOwner(owner100);
        musicSheet1.setPrice(10.0);
        musicSheet1.setComposer("Composer 1");

        musicSheet2 = new MusicSheet();
        musicSheet2.setId(1L);
        musicSheet2.setName("Test Sheet");
        musicSheet2.setDescription("A test music sheet");
        com.example.OLSHEETS.data.User owner100b = new com.example.OLSHEETS.data.User("owner100");
        owner100b.setId(100L);
        musicSheet2.setOwner(owner100b);
        musicSheet2.setPrice(10.0);
        musicSheet2.setComposer("Composer 1");

        instrument1 = new Instrument();
        instrument1.setId(2L);
        instrument1.setName("Test Instrument");
        instrument1.setDescription("A test instrument");
        com.example.OLSHEETS.data.User owner200 = new com.example.OLSHEETS.data.User("owner200");
        owner200.setId(200L);
        instrument1.setOwner(owner200);
        instrument1.setPrice(500.0);
        instrument1.setAge(5);
        instrument1.setType(InstrumentType.ACOUSTIC);
        instrument1.setFamily(InstrumentFamily.STRING);

        instrument2 = new Instrument();
        instrument2.setId(2L);
        instrument2.setName("Test Instrument");
        instrument2.setDescription("A test instrument");
        com.example.OLSHEETS.data.User owner200b = new com.example.OLSHEETS.data.User("owner200");
        owner200b.setId(200L);
        instrument2.setOwner(owner200b);
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
        
        com.example.OLSHEETS.data.User owner99 = new com.example.OLSHEETS.data.User("owner99");
        owner99.setId(99L);
        item.setOwner(owner99);
        assertThat(item.getOwner().getId()).isEqualTo(99L);
        
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
        com.example.OLSHEETS.data.User owner999 = new com.example.OLSHEETS.data.User("owner999");
        owner999.setId(999L);
        musicSheet2.setOwner(owner999);
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
        assertThat(musicSheet1).hasSameHashCodeAs(musicSheet2);
        assertThat(instrument1).hasSameHashCodeAs(instrument2);
    }

    @Test
    void testHashCodeWithDifferentValues() {
        musicSheet2.setName("Different Name");
        assertThat(musicSheet1.hashCode()).isNotEqualTo(musicSheet2.hashCode());
    }

    @Test
    void testHashCodeWithNullFields() {
        MusicSheet sheet = new MusicSheet();
        // hashCode should be calculated even with null fields
        assertThat(sheet).hasSameHashCodeAs(new MusicSheet());
    }

    @Test
    void testEqualsWithNullIdBothObjects() {
        MusicSheet sheet1 = new MusicSheet();
        sheet1.setName("Name");
        com.example.OLSHEETS.data.User owner1 = new com.example.OLSHEETS.data.User("owner1");
        owner1.setId(1L);
        sheet1.setOwner(owner1);
        sheet1.setPrice(10.0);
        
        MusicSheet sheet2 = new MusicSheet();
        sheet2.setName("Name");
        com.example.OLSHEETS.data.User owner1b = new com.example.OLSHEETS.data.User("owner1");
        owner1b.setId(1L);
        sheet2.setOwner(owner1b);
        sheet2.setPrice(10.0);
        
        assertThat(sheet1.equals(sheet2)).isTrue();
    }

    @Test
    void testEqualsWithNullNameBothObjects() {
        MusicSheet sheet1 = new MusicSheet();
        sheet1.setId(1L);
        com.example.OLSHEETS.data.User owner1c = new com.example.OLSHEETS.data.User("owner1");
        owner1c.setId(1L);
        sheet1.setOwner(owner1c);
        sheet1.setPrice(10.0);
        
        MusicSheet sheet2 = new MusicSheet();
        sheet2.setId(1L);
        com.example.OLSHEETS.data.User owner1d = new com.example.OLSHEETS.data.User("owner1");
        owner1d.setId(1L);
        sheet2.setOwner(owner1d);
        sheet2.setPrice(10.0);
        
        assertThat(sheet1.equals(sheet2)).isTrue();
    }

    @Test
    void testEqualsWithNullDescriptionBothObjects() {
        MusicSheet sheet1 = new MusicSheet();
        sheet1.setId(1L);
        sheet1.setName("Name");
        com.example.OLSHEETS.data.User owner1e = new com.example.OLSHEETS.data.User("owner1");
        owner1e.setId(1L);
        sheet1.setOwner(owner1e);
        sheet1.setPrice(10.0);
        
        MusicSheet sheet2 = new MusicSheet();
        sheet2.setId(1L);
        sheet2.setName("Name");
        com.example.OLSHEETS.data.User owner1f = new com.example.OLSHEETS.data.User("owner1");
        owner1f.setId(1L);
        sheet2.setOwner(owner1f);
        sheet2.setPrice(10.0);
        
        assertThat(sheet1.equals(sheet2)).isTrue();
    }

    @Test
    void testEqualsWithNullPriceBothObjects() {
        MusicSheet sheet1 = new MusicSheet();
        sheet1.setId(1L);
        sheet1.setName("Name");
        com.example.OLSHEETS.data.User owner1a = new com.example.OLSHEETS.data.User("owner1");
        owner1a.setId(1L);
        sheet1.setOwner(owner1a);
        
        MusicSheet sheet2 = new MusicSheet();
        sheet2.setId(1L);
        sheet2.setName("Name");
        com.example.OLSHEETS.data.User owner1b = new com.example.OLSHEETS.data.User("owner1");
        owner1b.setId(1L);
        sheet2.setOwner(owner1b);
        
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
        com.example.OLSHEETS.data.User ownerX = new com.example.OLSHEETS.data.User("owner1");
        ownerX.setId(1L);
        sheet.setOwner(ownerX);
        sheet.setPrice(10.0);
        
        Instrument instrument = new Instrument();
        instrument.setId(1L);
        instrument.setName("Test");
        com.example.OLSHEETS.data.User ownerY = new com.example.OLSHEETS.data.User("owner1");
        ownerY.setId(1L);
        instrument.setOwner(ownerY);
        instrument.setPrice(10.0);
        
        assertThat(sheet).isNotEqualTo(instrument);
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
        com.example.OLSHEETS.data.User ownerA = new com.example.OLSHEETS.data.User("owner1");
        ownerA.setId(1L);
        instr1.setOwner(ownerA);
        instr1.setPrice(null);
        
        Instrument instr2 = new Instrument();
        instr2.setId(1L);
        instr2.setName("Name");
        com.example.OLSHEETS.data.User ownerB = new com.example.OLSHEETS.data.User("owner1");
        ownerB.setId(1L);
        instr2.setOwner(ownerB);
        instr2.setPrice(10.0);
        
        assertThat(instr1).isNotEqualTo(instr2);
    }

    @Test
    void testDescriptionNullInSecondObjectButNotInFirst() {
        Instrument instr1 = new Instrument();
        instr1.setId(1L);
        instr1.setName("Name");
        com.example.OLSHEETS.data.User ownerC = new com.example.OLSHEETS.data.User("owner1");
        ownerC.setId(1L);
        instr1.setOwner(ownerC);
        instr1.setDescription("Description");
        
        Instrument instr2 = new Instrument();
        instr2.setId(1L);
        instr2.setName("Name");
        com.example.OLSHEETS.data.User ownerD = new com.example.OLSHEETS.data.User("owner1");
        ownerD.setId(1L);
        instr2.setOwner(ownerD);
        instr2.setDescription(null);
        
        assertThat(instr1).isNotEqualTo(instr2);
    }

    @Test
    void testIdNullInSecondObjectButNotInFirst() {
        Instrument instr1 = new Instrument();
        instr1.setId(1L);
        instr1.setName("Name");
        
        Instrument instr1 = new Instrument();
        instr1.setId(1L);
        instr1.setName("Name");
        com.example.OLSHEETS.data.User ownerE = new com.example.OLSHEETS.data.User("owner1");
        ownerE.setId(1L);
        instr1.setOwner(ownerE);
        assertThat(instr1).isNotEqualTo(instr2);
    }

    @Test
    void testNameNullInSecondObjectButNotInFirst() {
        Instrument instr2 = new Instrument();
        instr2.setId(null);
        instr2.setName("Name");
        com.example.OLSHEETS.data.User ownerF = new com.example.OLSHEETS.data.User("owner1");
        ownerF.setId(1L);
        instr2.setOwner(ownerF);
        Instrument instr2 = new Instrument();
        instr2.setId(1L);
        instr2.setName(null);
        
        assertThat(instr1).isNotEqualTo(instr2);
    }
}