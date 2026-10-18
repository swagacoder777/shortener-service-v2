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
@RestController
@RequestMapping("/api/links")
public class LinkController {

    private final LinkService linkService;

    private final RateLimitService rateLimitService;

    public LinkController(
            LinkService linkService,
            RateLimitService rateLimitService
    ) {
        this.linkService = linkService;
        this.rateLimitService = rateLimitService;
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
            @PathVariable String shortCode) {

        Link link = linkService.incrementClicks(shortCode);

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
}