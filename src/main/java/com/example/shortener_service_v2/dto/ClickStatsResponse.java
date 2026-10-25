package com.example.shortener_service_v2.dto;

public record ClickStatsResponse(
        String shortCode,
        long totalClicks
) {}