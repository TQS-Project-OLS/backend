package com.example.OLSHEETS.boundary;

import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.dto.PriceUpdateRequest;
import com.example.OLSHEETS.dto.PriceUpdateResponse;
import com.example.OLSHEETS.dto.PriceResponse;
import com.example.OLSHEETS.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class PricingController {

    @Autowired
    private ProductsService productsService;

    /**
     * Update the price of an item (instrument or music sheet)
     * PUT /api/items/price/{itemId}
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
     * Get the current price of an item
     * GET /api/items/price/{itemId}
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
