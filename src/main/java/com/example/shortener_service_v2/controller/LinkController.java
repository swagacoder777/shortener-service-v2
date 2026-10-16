package com.example.shortener_service_v2.controller;

import com.example.shortener_service_v2.entity.Link;
import com.example.shortener_service_v2.service.LinkService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
@RestController
@RequestMapping("/api/links")
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Link createLink(@Valid @RequestBody CreateLinkRequest request) {
        return linkService.createLink(request.originalUrl());
    }

    public record CreateLinkRequest(
            @NotBlank
            @URL
            String originalUrl
    ) {
    }
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        Link link = linkService.getByShortCode(shortCode);

        return ResponseEntity
                .status(HttpStatus.FOUND)
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