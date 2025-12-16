package com.example.OLSHEETS.boundary;

import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.dto.PriceUpdateRequest;
import com.example.OLSHEETS.dto.PriceUpdateResponse;
import com.example.OLSHEETS.dto.PriceResponse;
import com.example.OLSHEETS.exception.UserNotFoundException;
import com.example.OLSHEETS.repository.UserRepository;
import com.example.OLSHEETS.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class PricingController {

    private final ProductsService productsService;

    @Autowired
    private UserRepository userRepository;

    public PricingController(ProductsService productsService) {
        this.productsService = productsService;
    }

    /**
     * Update the price of an item (instrument or music sheet)
     * PUT /api/items/price/{itemId}
     */
    @PutMapping("/price/{itemId}")
    public ResponseEntity<?> updatePrice(
            @PathVariable Long itemId,
            @RequestBody PriceUpdateRequest request) {
        try {
            // Extract username from JWT token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Get user from database
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            Item updatedItem = productsService.updateItemPrice(itemId, request.getNewPrice(), user.getId());
            return ResponseEntity.ok(new PriceUpdateResponse(
                updatedItem.getId(), 
                updatedItem.getName(), 
                updatedItem.getPrice()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
