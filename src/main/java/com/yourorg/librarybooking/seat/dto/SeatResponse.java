package com.yourorg.librarybooking.seat.dto;

public record SeatResponse(
    Long id,
    String seatNumber,
    Long zoneId,
    boolean isActive
) {}
