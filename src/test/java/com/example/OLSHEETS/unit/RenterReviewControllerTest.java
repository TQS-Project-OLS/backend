package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.controller.RenterReviewController;
import com.example.OLSHEETS.dto.RenterReviewRequest;
import com.example.OLSHEETS.dto.RenterReviewResponse;
import com.example.OLSHEETS.service.RenterReviewService;
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
import com.example.OLSHEETS.security.JwtUtil;
import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

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

@WebMvcTest(controllers = RenterReviewController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(RenterReviewControllerTest.TestConfig.class)
class RenterReviewControllerTest {

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
    private RenterReviewService renterReviewService;

    private RenterReviewResponse renterReviewResponse1;
    private RenterReviewResponse renterReviewResponse2;

    @BeforeEach
    void setUp() {
        renterReviewResponse1 = new RenterReviewResponse(
            1L,              // id
            1L,              // bookingId
            1L,              // renterId
            "renter1",       // renterName
            1L,              // itemId
            "Guitar",        // itemName
            1L,              // ownerId
            5,               // score
            "Great renter, very responsible!",  // comment
            LocalDateTime.now()  // createdAt
        );

        renterReviewResponse2 = new RenterReviewResponse(
            2L,              // id
            2L,              // bookingId
            1L,              // renterId
            "renter1",       // renterName
            2L,              // itemId
            "Piano",         // itemName
            2L,              // ownerId
            4,               // score
            "Good experience",  // comment
            LocalDateTime.now()  // createdAt
        );
    }

    @Test
    @Requirement("OLS-63")
    void testCreateRenterReview_WithValidData_ShouldReturnCreated() throws Exception {
        RenterReviewRequest request = new RenterReviewRequest(1L, 5, "Excellent renter!");
        
        when(renterReviewService.createRenterReview(any(RenterReviewRequest.class), eq(1L)))
            .thenReturn(renterReviewResponse1);

        mockMvc.perform(post("/api/renter-reviews")
                        .param("ownerId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.score", is(5)))
                .andExpect(jsonPath("$.comment", is("Great renter, very responsible!")));

        verify(renterReviewService, times(1)).createRenterReview(any(RenterReviewRequest.class), eq(1L));
    }

    @Test
    @Requirement("OLS-63")
    void testCreateRenterReview_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        RenterReviewRequest request = new RenterReviewRequest(1L, 6, "Invalid score");
        
        when(renterReviewService.createRenterReview(any(RenterReviewRequest.class), eq(1L)))
            .thenThrow(new IllegalArgumentException("Score must be between 1 and 5"));

        mockMvc.perform(post("/api/renter-reviews")
                        .param("ownerId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(renterReviewService, times(1)).createRenterReview(any(RenterReviewRequest.class), eq(1L));
    }

    @Test
    @Requirement("OLS-63")
    void testCreateRenterReview_WithBookingNotCompleted_ShouldReturnBadRequest() throws Exception {
        RenterReviewRequest request = new RenterReviewRequest(1L, 5, "Good renter");
        
        when(renterReviewService.createRenterReview(any(RenterReviewRequest.class), eq(1L)))
            .thenThrow(new IllegalArgumentException("Booking is not completed yet"));

        mockMvc.perform(post("/api/renter-reviews")
                        .param("ownerId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(renterReviewService, times(1)).createRenterReview(any(RenterReviewRequest.class), eq(1L));
    }

    @Test
    @Requirement("OLS-63")
    void testCreateRenterReview_WithAlreadyReviewed_ShouldReturnBadRequest() throws Exception {
        RenterReviewRequest request = new RenterReviewRequest(1L, 5, "Good renter");
        
        when(renterReviewService.createRenterReview(any(RenterReviewRequest.class), eq(1L)))
            .thenThrow(new IllegalArgumentException("Booking already reviewed"));

        mockMvc.perform(post("/api/renter-reviews")
                        .param("ownerId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(renterReviewService, times(1)).createRenterReview(any(RenterReviewRequest.class), eq(1L));
    }

    @Test
    @Requirement("OLS-63")
    void testGetReviewsByRenterId_ShouldReturnReviewsList() throws Exception {
        List<RenterReviewResponse> reviews = Arrays.asList(renterReviewResponse1, renterReviewResponse2);
        
        when(renterReviewService.getReviewsByRenterId(1L)).thenReturn(reviews);

        mockMvc.perform(get("/api/renter-reviews/renter/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].score", is(5)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].score", is(4)));

        verify(renterReviewService, times(1)).getReviewsByRenterId(1L);
    }

    @Test
    @Requirement("OLS-63")
    void testGetReviewsByRenterId_WithNoReviews_ShouldReturnEmptyList() throws Exception {
        when(renterReviewService.getReviewsByRenterId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/renter-reviews/renter/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(renterReviewService, times(1)).getReviewsByRenterId(1L);
    }

    @Test
    @Requirement("OLS-63")
    void testGetAverageScoreByRenterId_ShouldReturnAverage() throws Exception {
        when(renterReviewService.getAverageScoreByRenterId(1L)).thenReturn(4.5);

        mockMvc.perform(get("/api/renter-reviews/renter/1/average"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(4.5)));

        verify(renterReviewService, times(1)).getAverageScoreByRenterId(1L);
    }

    @Test
    @Requirement("OLS-63")
    void testGetAverageScoreByRenterId_WithNoReviews_ShouldReturnZero() throws Exception {
        when(renterReviewService.getAverageScoreByRenterId(1L)).thenReturn(0.0);

        mockMvc.perform(get("/api/renter-reviews/renter/1/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(0.0)));

        verify(renterReviewService, times(1)).getAverageScoreByRenterId(1L);
    }

    @Test
    @Requirement("OLS-63")
    void testGetRenterReviewByBookingId_WithExistingReview_ShouldReturnReview() throws Exception {
        when(renterReviewService.getRenterReviewByBookingId(1L)).thenReturn(renterReviewResponse1);

        mockMvc.perform(get("/api/renter-reviews/booking/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.bookingId", is(1)))
                .andExpect(jsonPath("$.score", is(5)))
                .andExpect(jsonPath("$.comment", is("Great renter, very responsible!")));

        verify(renterReviewService, times(1)).getRenterReviewByBookingId(1L);
    }

    @Test
    @Requirement("OLS-63")
    void testGetRenterReviewByBookingId_WithNonExistentReview_ShouldReturnNotFound() throws Exception {
        when(renterReviewService.getRenterReviewByBookingId(999L))
            .thenThrow(new IllegalArgumentException("No review found"));

        mockMvc.perform(get("/api/renter-reviews/booking/999"))
                .andExpect(status().isNotFound());

        verify(renterReviewService, times(1)).getRenterReviewByBookingId(999L);
    }

    @Test
    @Requirement("OLS-63")
    void testCanReviewRenter_WhenAllowed_ShouldReturnTrue() throws Exception {
        when(renterReviewService.canReviewRenter(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/api/renter-reviews/can-review")
                        .param("bookingId", "1")
                        .param("ownerId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(true)));

        verify(renterReviewService, times(1)).canReviewRenter(1L, 1L);
    }

    @Test
    @Requirement("OLS-63")
    void testCanReviewRenter_WhenNotAllowed_ShouldReturnFalse() throws Exception {
        when(renterReviewService.canReviewRenter(1L, 1L)).thenReturn(false);

        mockMvc.perform(get("/api/renter-reviews/can-review")
                        .param("bookingId", "1")
                        .param("ownerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(false)));

        verify(renterReviewService, times(1)).canReviewRenter(1L, 1L);
    }

    @Test
    @Requirement("OLS-63")
    void testCanReviewRenter_WithNonOwnerUser_ShouldReturnFalse() throws Exception {
        when(renterReviewService.canReviewRenter(1L, 2L)).thenReturn(false);

        mockMvc.perform(get("/api/renter-reviews/can-review")
                        .param("bookingId", "1")
                        .param("ownerId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(false)));

        verify(renterReviewService, times(1)).canReviewRenter(1L, 2L);
    }
}
