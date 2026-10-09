package com.yourorg.librarybooking.zone.dto;

import java.math.BigDecimal;

public record ZoneResponse(
    Long id,
    String name,
    String description,
    Integer capacity,
    BigDecimal pricePerHour,
    String imageUrl
) {}
