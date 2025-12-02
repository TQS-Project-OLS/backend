package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.boundary.SheetsController;
import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.SheetCategory;
import com.example.OLSHEETS.service.ProductsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SheetsController.class)
class SheetsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductsService productsService;

    private MusicSheet sheet1;
    private MusicSheet sheet2;

    @BeforeEach
    void setUp() {
        sheet1 = new MusicSheet();
        sheet1.setId(1L);
        sheet1.setName("Moonlight Sonata");
        sheet1.setComposer("Beethoven");
        sheet1.setCategory(SheetCategory.CLASSICAL);
        sheet1.setDescription("Piano Sonata No. 14");
        sheet1.setPrice(9.99);
        sheet1.setOwnerId(1);

        sheet2 = new MusicSheet();
        sheet2.setId(2L);
        sheet2.setName("Bohemian Rhapsody");
        sheet2.setComposer("Freddie Mercury");
        sheet2.setCategory(SheetCategory.ROCK);
        sheet2.setDescription("Queen masterpiece");
        sheet2.setPrice(12.99);
        sheet2.setOwnerId(1);
    }

    @Test
    void testSearchSheets_WithMatchingResults_ShouldReturnSheetsList() throws Exception {
        List<MusicSheet> sheets = Collections.singletonList(sheet1);
        when(productsService.searchMusicSheetsByName("Moonlight")).thenReturn(sheets);

        mockMvc.perform(get("/api/sheets/search")
                        .param("name", "Moonlight"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Moonlight Sonata")))
                .andExpect(jsonPath("$[0].composer", is("Beethoven")))
                .andExpect(jsonPath("$[0].category", is("CLASSICAL")))
                .andExpect(jsonPath("$[0].price", is(9.99)));

        verify(productsService, times(1)).searchMusicSheetsByName("Moonlight");
    }

    @Test
    void testSearchSheets_WithNoResults_ShouldReturnEmptyList() throws Exception {
        when(productsService.searchMusicSheetsByName("Symphony")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/sheets/search")
                        .param("name", "Symphony"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(productsService, times(1)).searchMusicSheetsByName("Symphony");
    }

    @Test
    void testSearchSheets_WithMultipleResults_ShouldReturnAllSheets() throws Exception {
        List<MusicSheet> sheets = Arrays.asList(sheet1, sheet2);
        when(productsService.searchMusicSheetsByName("a")).thenReturn(sheets);

        mockMvc.perform(get("/api/sheets/search")
                        .param("name", "a"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(productsService, times(1)).searchMusicSheetsByName("a");
    }

    @Test
    void testFilterByCategory_WithMatchingResults_ShouldReturnSheetsList() throws Exception {
        List<MusicSheet> sheets = Collections.singletonList(sheet1);
        when(productsService.filterMusicSheetsByCategory(SheetCategory.CLASSICAL)).thenReturn(sheets);

        mockMvc.perform(get("/api/sheets/filter/category")
                        .param("category", "CLASSICAL"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Moonlight Sonata")))
                .andExpect(jsonPath("$[0].category", is("CLASSICAL")));

        verify(productsService, times(1)).filterMusicSheetsByCategory(SheetCategory.CLASSICAL);
    }

    @Test
    void testFilterByCategory_WithNoResults_ShouldReturnEmptyList() throws Exception {
        when(productsService.filterMusicSheetsByCategory(SheetCategory.JAZZ)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/sheets/filter/category")
                        .param("category", "JAZZ"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(productsService, times(1)).filterMusicSheetsByCategory(SheetCategory.JAZZ);
    }

    @Test
    void testFilterByCategory_WithRockCategory_ShouldReturnRockSheets() throws Exception {
        List<MusicSheet> sheets = Collections.singletonList(sheet2);
        when(productsService.filterMusicSheetsByCategory(SheetCategory.ROCK)).thenReturn(sheets);

        mockMvc.perform(get("/api/sheets/filter/category")
                        .param("category", "ROCK"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Bohemian Rhapsody")))
                .andExpect(jsonPath("$[0].composer", is("Freddie Mercury")))
                .andExpect(jsonPath("$[0].category", is("ROCK")));

        verify(productsService, times(1)).filterMusicSheetsByCategory(SheetCategory.ROCK);
    }

    @Test
    void testSearchSheets_VerifyAllFieldsReturned() throws Exception {
        List<MusicSheet> sheets = Collections.singletonList(sheet1);
        when(productsService.searchMusicSheetsByName("Sonata")).thenReturn(sheets);

        mockMvc.perform(get("/api/sheets/search")
                        .param("name", "Sonata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Moonlight Sonata")))
                .andExpect(jsonPath("$[0].composer", is("Beethoven")))
                .andExpect(jsonPath("$[0].category", is("CLASSICAL")))
                .andExpect(jsonPath("$[0].description", is("Piano Sonata No. 14")))
                .andExpect(jsonPath("$[0].price", is(9.99)))
                .andExpect(jsonPath("$[0].ownerId", is(1)));
    }
}
