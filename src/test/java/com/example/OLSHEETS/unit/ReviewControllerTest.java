package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.boundary.ReviewController;
import com.example.OLSHEETS.dto.ReviewRequest;
import com.example.OLSHEETS.dto.ReviewResponse;
import com.example.OLSHEETS.security.JwtUtil;
import com.example.OLSHEETS.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReviewController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(ReviewControllerTest.TestConfig.class)
class ReviewControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtUtil jwtUtil() {
            return org.mockito.Mockito.mock(JwtUtil.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    private ReviewResponse reviewResponse1;
    private ReviewResponse reviewResponse2;

    @BeforeEach
    void setUp() {
        reviewResponse1 = new ReviewResponse(
            1L, 1L, 1L, "Yamaha Piano", 
            1L, "john_doe", 5, 
            "Excellent instrument!", LocalDateTime.now()
        );

        reviewResponse2 = new ReviewResponse(
            2L, 2L, 1L, "Yamaha Piano", 
            2L, "jane_smith", 4, 
            "Good quality", LocalDateTime.now()
        );
    }

    @Test
    void testCreateReview_WithValidData_ShouldReturnCreated() throws Exception {
        ReviewRequest request = new ReviewRequest(1L, 5, "Great experience!");
        
        when(reviewService.createReview(any(ReviewRequest.class), eq(1L)))
            .thenReturn(reviewResponse1);

        mockMvc.perform(post("/api/reviews")
                        .param("renterId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.score", is(5)))
                .andExpect(jsonPath("$.comment", is("Excellent instrument!")));

        verify(reviewService, times(1)).createReview(any(ReviewRequest.class), eq(1L));
    }

    @Test
    void testCreateReview_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        ReviewRequest request = new ReviewRequest(1L, 6, "Invalid score");
        
        when(reviewService.createReview(any(ReviewRequest.class), eq(1L)))
            .thenThrow(new IllegalArgumentException("Score must be between 1 and 5"));

        mockMvc.perform(post("/api/reviews")
                        .param("renterId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(reviewService, times(1)).createReview(any(ReviewRequest.class), eq(1L));
    }

    @Test
    void testGetReviewsByItem_ShouldReturnReviewsList() throws Exception {
        List<ReviewResponse> reviews = Arrays.asList(reviewResponse1, reviewResponse2);
        
        when(reviewService.getReviewsByItemId(1L)).thenReturn(reviews);

        mockMvc.perform(get("/api/reviews/item/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].score", is(5)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].score", is(4)));

        verify(reviewService, times(1)).getReviewsByItemId(1L);
    }

    @Test
    void testGetReviewsByItem_WithNoReviews_ShouldReturnEmptyList() throws Exception {
        when(reviewService.getReviewsByItemId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/reviews/item/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(reviewService, times(1)).getReviewsByItemId(1L);
    }

    @Test
    void testGetAverageScore_ShouldReturnAverage() throws Exception {
        when(reviewService.getAverageScoreByItemId(1L)).thenReturn(4.5);

        mockMvc.perform(get("/api/reviews/item/1/average"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.averageScore", is(4.5)));

        verify(reviewService, times(1)).getAverageScoreByItemId(1L);
    }

    @Test
    void testGetAverageScore_WithNoReviews_ShouldReturnZero() throws Exception {
        when(reviewService.getAverageScoreByItemId(1L)).thenReturn(0.0);

        mockMvc.perform(get("/api/reviews/item/1/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageScore", is(0.0)));

        verify(reviewService, times(1)).getAverageScoreByItemId(1L);
    }

    @Test
    void testGetReviewByBooking_WithExistingReview_ShouldReturnReview() throws Exception {
        when(reviewService.getReviewByBookingId(1L)).thenReturn(reviewResponse1);

        mockMvc.perform(get("/api/reviews/booking/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.bookingId", is(1)))
                .andExpect(jsonPath("$.score", is(5)));

        verify(reviewService, times(1)).getReviewByBookingId(1L);
    }

    @Test
    void testGetReviewByBooking_WithNonExistentReview_ShouldReturnNotFound() throws Exception {
        when(reviewService.getReviewByBookingId(999L))
            .thenThrow(new IllegalArgumentException("No review found"));

        mockMvc.perform(get("/api/reviews/booking/999"))
                .andExpect(status().isNotFound());

        verify(reviewService, times(1)).getReviewByBookingId(999L);
    }

    @Test
    void testCanReview_WhenAllowed_ShouldReturnTrue() throws Exception {
        when(reviewService.canReviewBooking(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/api/reviews/booking/1/can-review")
                        .param("renterId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.canReview", is(true)));

        verify(reviewService, times(1)).canReviewBooking(1L, 1L);
    }

    @Test
    void testCanReview_WhenNotAllowed_ShouldReturnFalse() throws Exception {
        when(reviewService.canReviewBooking(1L, 1L)).thenReturn(false);

        mockMvc.perform(get("/api/reviews/booking/1/can-review")
                        .param("renterId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canReview", is(false)));

        verify(reviewService, times(1)).canReviewBooking(1L, 1L);
    }
}
