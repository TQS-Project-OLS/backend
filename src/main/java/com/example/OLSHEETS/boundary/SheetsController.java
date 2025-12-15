package com.example.OLSHEETS.boundary;

import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.dto.MusicSheetRegistrationRequest;
import com.example.OLSHEETS.exception.UserNotFoundException;
import com.example.OLSHEETS.repository.UserRepository;
import com.example.OLSHEETS.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sheets")
public class SheetsController {

    @Autowired
    private ProductsService productsService;

    @Autowired
    private UserRepository userRepository;

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

    @PostMapping("/register")
    public ResponseEntity<?> registerMusicSheet(@RequestBody MusicSheetRegistrationRequest request) {
        try {
            // If ownerId is not already set in request, extract from authentication
            if (request.getOwnerId() == null) {
                // Extract username from JWT token
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName();

                // Get user from database
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UserNotFoundException("User not found"));

                // Set ownerId from authenticated user
                request.setOwnerId(user.getId());
            }

            MusicSheet registeredMusicSheet = productsService.registerMusicSheet(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredMusicSheet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
