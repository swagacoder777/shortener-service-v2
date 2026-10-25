package com.example.shortener_service_v2.controller;

import com.example.shortener_service_v2.entity.Link;
import com.example.shortener_service_v2.service.LinkService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import com.example.shortener_service_v2.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import com.example.shortener_service_v2.event.LinkClickedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import java.time.Instant;
import java.util.UUID;
import com.example.shortener_service_v2.client.AnalyticsClient;
import com.example.shortener_service_v2.dto.ClickStatsResponse;
@RestController
@RequestMapping("/api/links")
public class LinkController {

    private final LinkService linkService;
    private final KafkaTemplate<String, LinkClickedEvent> kafkaTemplate;

    private final RateLimitService rateLimitService;
    private final AnalyticsClient analyticsClient;

    public LinkController(
            LinkService linkService,
            RateLimitService rateLimitService,
            KafkaTemplate<String, LinkClickedEvent> kafkaTemplate,
            AnalyticsClient analyticsClient
    ) {
        this.linkService = linkService;
        this.rateLimitService = rateLimitService;
        this.kafkaTemplate = kafkaTemplate;
        this.analyticsClient = analyticsClient;
    }

    @PostMapping
    public ResponseEntity<Link> createLink(
            @Valid @RequestBody CreateLinkRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = httpRequest.getRemoteAddr();

        if (rateLimitService.isRateLimited(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        Link link = linkService.createLink(request.originalUrl());

        return ResponseEntity.status(HttpStatus.CREATED).body(link);
    }

    public record CreateLinkRequest(
            @NotBlank
            @URL
            String originalUrl
    ) {
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToOriginal(
            @PathVariable String shortCode,
            HttpServletRequest httpRequest
    ) {
        Link link = linkService.incrementClicks(shortCode);

        String userAgent = httpRequest.getHeader("User-Agent");
        String correlationId = httpRequest.getHeader("X-Correlation-ID");

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        LinkClickedEvent event = new LinkClickedEvent(
                shortCode,
                link.getOriginalUrl(),
                Instant.now(),
                userAgent,
                correlationId
        );

        kafkaTemplate.send("link-clicks", shortCode, event);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", link.getOriginalUrl())
                .build();
    }

    @DeleteMapping("/{shortCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLink(@PathVariable String shortCode) {
        linkService.deleteByShortCode(shortCode);
    }

    @GetMapping("/{shortCode}/stats")
    public Link getStats(@PathVariable String shortCode) {
        return linkService.getStats(shortCode);
    }
    @GetMapping("/{shortCode}/analytics")
    public ClickStatsResponse getAnalytics(@PathVariable String shortCode) {
        return analyticsClient.getStats(shortCode);
    }
}
