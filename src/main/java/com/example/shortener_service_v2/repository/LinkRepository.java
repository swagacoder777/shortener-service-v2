package com.example.shortener_service_v2.repository;

import com.example.shortener_service_v2.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByShortCode(String shortCode);
}
