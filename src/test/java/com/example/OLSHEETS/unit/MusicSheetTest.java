package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.MusicSheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class MusicSheetTest {

    private MusicSheet musicSheet1;
    private MusicSheet musicSheet2;

    @BeforeEach
    void setUp() {
        musicSheet1 = new MusicSheet();
        musicSheet1.setId(1L);
        musicSheet1.setTitle("Moonlight Sonata");
        musicSheet1.setCategory("Classical");
        musicSheet1.setComposer("Beethoven");
        com.example.OLSHEETS.data.User owner100 = new com.example.OLSHEETS.data.User("owner100", "owner100@example.com", "owner100");
        owner100.setId(100L);
        musicSheet1.setOwner(owner100);
        musicSheet1.setDescription("Beautiful piece");
        musicSheet1.setPrice(15.99);
        musicSheet1.setInstrumentation("Piano");
        musicSheet1.setDuration(14.5f);

        musicSheet2 = new MusicSheet();
        musicSheet2.setId(1L);
        musicSheet2.setTitle("Moonlight Sonata");
        musicSheet2.setCategory("Classical");
        musicSheet2.setComposer("Beethoven");
        com.example.OLSHEETS.data.User owner100b = new com.example.OLSHEETS.data.User("owner100", "owner100@example.com", "owner100");
        owner100b.setId(100L);
        musicSheet2.setOwner(owner100b);
        musicSheet2.setDescription("Beautiful piece");
        musicSheet2.setPrice(15.99);
        musicSheet2.setInstrumentation("Piano");
        musicSheet2.setDuration(14.5f);
    }

    @Test
    void testDefaultConstructor() {
        MusicSheet sheet = new MusicSheet();
        assertThat(sheet).isNotNull();
        assertThat(sheet.getComposer()).isNull();
        assertThat(sheet.getCategory()).isNull();
        assertThat(sheet.getInstrumentation()).isNull();
        assertThat(sheet.getDuration()).isNull();
    }

    @Test
    void testParameterizedConstructor() {
        com.example.OLSHEETS.data.User owner200 = new com.example.OLSHEETS.data.User("owner200", "owner200@example.com", "owner200");
        owner200.setId(200L);
        MusicSheet sheet = new MusicSheet("Fur Elise", "Classical", "Beethoven", owner200);
        
        assertThat(sheet.getTitle()).isEqualTo("Fur Elise");
        assertThat(sheet.getName()).isEqualTo("Fur Elise");
        assertThat(sheet.getCategory()).isEqualTo("Classical");
        assertThat(sheet.getComposer()).isEqualTo("Beethoven");
        assertThat(sheet.getOwner().getId()).isEqualTo(200L);
    }

    @Test
    void testComposerGetterAndSetter() {
        MusicSheet sheet = new MusicSheet();
        sheet.setComposer("Mozart");
        assertThat(sheet.getComposer()).isEqualTo("Mozart");
    }

    @Test
    void testInstrumentationGetterAndSetter() {
        MusicSheet sheet = new MusicSheet();
        sheet.setInstrumentation("Piano and Violin");
        assertThat(sheet.getInstrumentation()).isEqualTo("Piano and Violin");
    }

    @Test
    void testCategoryGetterAndSetter() {
        MusicSheet sheet = new MusicSheet();
        sheet.setCategory("Jazz");
        assertThat(sheet.getCategory()).isEqualTo("Jazz");
    }

    @Test
    void testDurationGetterAndSetter() {
        MusicSheet sheet = new MusicSheet();
        sheet.setDuration(5.5f);
        assertThat(sheet.getDuration()).isEqualTo(5.5f);
    }

    @Test
    void testTitleGetterAndSetter() {
        MusicSheet sheet = new MusicSheet();
        sheet.setTitle("Test Title");
        assertThat(sheet.getTitle()).isEqualTo("Test Title");
        assertThat(sheet.getName()).isEqualTo("Test Title");
    }

    @Test
    void testOwnerIdSetterWithLong() {
        MusicSheet sheet = new MusicSheet();
        com.example.OLSHEETS.data.User owner500 = new com.example.OLSHEETS.data.User("owner500", "owner500@example.com", "owner500");
        owner500.setId(500L);
        sheet.setOwner(owner500);
        assertThat(sheet.getOwner().getId()).isEqualTo(500L);
    }

    @Test
    void testEqualsWithSameObject() {
        assertThat(musicSheet1.equals(musicSheet1)).isTrue();
    }

    @Test
    void testEqualsWithEqualObjects() {
        assertThat(musicSheet1.equals(musicSheet2)).isTrue();
        assertThat(musicSheet2.equals(musicSheet1)).isTrue();
    }

    @Test
    void testEqualsWithNull() {
        assertThat(musicSheet1.equals(null)).isFalse();
    }

    @Test
    void testEqualsWithDifferentClass() {
        assertThat(musicSheet1.equals("String")).isFalse();
        assertThat(musicSheet1.equals(new Object())).isFalse();
    }

    @Test
    void testEqualsWithDifferentComposer() {
        musicSheet2.setComposer("Mozart");
        assertThat(musicSheet1.equals(musicSheet2)).isFalse();
    }

    @Test
    void testEqualsWithDifferentInstrumentation() {
        musicSheet2.setInstrumentation("Orchestra");
        assertThat(musicSheet1.equals(musicSheet2)).isFalse();
    }

    @Test
    void testEqualsWithDifferentCategory() {
        musicSheet2.setCategory("Jazz");
        assertThat(musicSheet1.equals(musicSheet2)).isFalse();
    }

    @Test
    void testEqualsWithDifferentDuration() {
        musicSheet2.setDuration(20.0f);
        assertThat(musicSheet1.equals(musicSheet2)).isFalse();
    }

    @Test
    void testEqualsWithNullFields() {
        MusicSheet sheet1 = new MusicSheet();
        sheet1.setTitle("Title");
        sheet1.setCategory("Category");
        
        MusicSheet sheet2 = new MusicSheet();
        sheet2.setTitle("Title");
        sheet2.setCategory("Category");
        
        assertThat(sheet1.equals(sheet2)).isTrue();
        
        // Test one null vs non-null
        musicSheet2.setComposer(null);
        assertThat(musicSheet1.equals(musicSheet2)).isFalse();
        
        musicSheet1.setInstrumentation(null);
        assertThat(musicSheet1.equals(musicSheet2)).isFalse();
    }

    @Test
    void testEqualsCallsSuperEquals() {
        musicSheet2.setTitle("Different Title");
        assertThat(musicSheet1.equals(musicSheet2)).isFalse();
    }

    @Test
    void testHashCodeConsistency() {
        assertThat(musicSheet1).hasSameHashCodeAs(musicSheet2);
        
        musicSheet2.setComposer("Different Composer");
        assertThat(musicSheet1.hashCode()).isNotEqualTo(musicSheet2.hashCode());
        
        MusicSheet sheet = new MusicSheet();
        assertThat(sheet).hasSameHashCodeAs(new MusicSheet());
    }

    @Test
    void testTitleAndNameConsistency() {
        MusicSheet sheet = new MusicSheet();
        sheet.setTitle("Test");
        assertThat(sheet.getName()).isEqualTo("Test");
        
        sheet.setName("Another Test");
        assertThat(sheet.getTitle()).isEqualTo("Another Test");
    }
}