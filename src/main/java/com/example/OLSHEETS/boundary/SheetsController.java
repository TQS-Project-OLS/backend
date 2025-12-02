package com.example.OLSHEETS.boundary;

import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.dto.PriceUpdateRequest;
import com.example.OLSHEETS.dto.PriceUpdateResponse;
import com.example.OLSHEETS.dto.PriceResponse;
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

    // Pricing management endpoints

    /**
     * Update the price of a music sheet
     * PUT /api/sheets/price/{itemId}
     */
    @PutMapping("/price/{itemId}")
    public ResponseEntity<PriceUpdateResponse> updatePrice(
            @PathVariable Long itemId,
            @RequestBody PriceUpdateRequest request) {
        try {
            Item updatedItem = productsService.updateItemPrice(itemId, request.getNewPrice());
            return ResponseEntity.ok(new PriceUpdateResponse(
                updatedItem.getId(), 
                updatedItem.getName(), 
                updatedItem.getPrice()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get the current price of a music sheet
     * GET /api/sheets/price/{itemId}
     */
    @GetMapping("/price/{itemId}")
    public ResponseEntity<PriceResponse> getPrice(@PathVariable Long itemId) {
        try {
            Double price = productsService.getItemPrice(itemId);
            return ResponseEntity.ok(new PriceResponse(itemId, price));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
