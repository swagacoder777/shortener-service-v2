package com.example.shortener_service_v2.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.shortener_service_v2.dto.ClickStatsResponse;
import java.time.Duration;
import reactor.core.publisher.Mono;
@Component
public class AnalyticsClient {

    private final WebClient webClient;

    public AnalyticsClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://analytics-service:8082")
                .build();
    }

    public ClickStatsResponse getStats(String shortCode) {
        return webClient.get()
                .uri("/api/analytics/clicks/{shortCode}/count", shortCode)
                .retrieve()
                .bodyToMono(Long.class)
                .timeout(Duration.ofSeconds(3))
                .map(count -> new ClickStatsResponse(shortCode, count))
                .onErrorResume(error ->
                        Mono.just(new ClickStatsResponse(shortCode, 0L)))
                .block();
    }


}
