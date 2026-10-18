package com.example.shortener_service_v2.service;

import com.example.shortener_service_v2.entity.Link;
import com.example.shortener_service_v2.repository.LinkRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.NoSuchElementException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
@Service
public class LinkService {

    private final LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }
    public Link createLink(String originalUrl) {
        Link link = new Link();

        link.setOriginalUrl(originalUrl);
        link.setShortCode(UUID.randomUUID().toString().substring(0, 8));
        link.setCreatedAt(LocalDateTime.now());

        return linkRepository.save(link);
    }
    @Cacheable(value = "links", key = "#shortCode")
    public Link getByShortCode(String shortCode) {
        return linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new NoSuchElementException("Link not found"));
    }
    @CacheEvict(value = "links", key = "#shortCode")
    public void deleteByShortCode(String shortCode) {
        Link link = getByShortCode(shortCode);
        linkRepository.delete(link);
    }

    public Link getStats(String shortCode) {
        return getByShortCode(shortCode);
    }

    @CachePut(value = "links", key = "#shortCode")
    public Link incrementClicks(String shortCode) {
        Link link = getByShortCode(shortCode);
        link.setClicks(link.getClicks() + 1);
        return linkRepository.save(link);
    }
}