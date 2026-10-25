package com.example.shortener_service_v2.event;

import java.time.Instant;

public record LinkClickedEvent(
        String shortCode,
        String originalUrl,
        Instant clickedAt,
        String userAgent,
        String correlationId
) {
}