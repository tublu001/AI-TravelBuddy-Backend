package com.dev.openai.travelassistant.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

//@Service
public class AzureGptService {

    @Value("${azure.gpt.api.subscriptionKey}")
    private String apiKey;

    @Value("${azure.gpt.api.baseUrl}")
    private String apiUrl;

    private final WebClient webClient;

    public AzureGptService() {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("api-key", apiKey)
                .build();
    }

    public Mono<String> generateText(String inputText) {
        // Create a JSON request body with your input text
        String requestBody = "{\"input\": \"" + inputText + "\"}";

        return webClient.post()
                .uri("/openai/deployments/Mahadev_GPT_35/chat/completions?api-version=2023-07-01-preview") // Replace with the actual API endpoint path
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(String.class);
    }
}
