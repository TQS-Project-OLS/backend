package com.example.OLSHEETS.data;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class SheetBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sheet_id")
    private MusicSheet musicSheet;
    @ManyToOne
    @JoinColumn(name = "renter_id")
    private User renter;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    public SheetBooking() {}

    public SheetBooking(MusicSheet musicSheet, User renter, LocalDate startDate, LocalDate endDate) {
        this.musicSheet = musicSheet;
        this.renter = renter;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = BookingStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MusicSheet getMusicSheet() {
        return musicSheet;
    }

    public void setMusicSheet(MusicSheet musicSheet) {
        this.musicSheet = musicSheet;
    }

    public User getRenter() {
        return renter;
    }

    public void setRenter(User renter) {
        this.renter = renter;
    }


    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}

