package com.example.OLSHEETS.boundary;

import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.SheetCategory;
import com.example.OLSHEETS.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sheets")
public class SheetsController {

    @Autowired
    private ProductsService productsService;

    @GetMapping("/search")
    public ResponseEntity<List<MusicSheet>> searchSheets(@RequestParam String name) {
        List<MusicSheet> sheets = productsService.searchMusicSheetsByName(name);
        return ResponseEntity.ok(sheets);
    }

    @GetMapping("/filter/category")
    public ResponseEntity<List<MusicSheet>> filterByCategory(@RequestParam String category) {
        List<MusicSheet> sheets = productsService.filterMusicSheetsByCategory(category);
        return ResponseEntity.ok(sheets);
    }
}
