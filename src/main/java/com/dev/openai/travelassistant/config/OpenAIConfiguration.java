package com.dev.openai.travelassistant.config;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class OpenAIConfiguration {

    @Value("${openai.key}")
    private String apiKey;

    @Value("${openai.timeout}")
    private int apiTimeout;

    @Value("${openai.temperature}")
    private double apiTemperature;

    @Value("${openai.model}")
    private String gptModel = "gpt-3.5-turbo";

    @Bean
    public OpenAiService initializeOpenAIService() {
        OpenAiService openAiService = new OpenAiService(apiKey, Duration.ofSeconds(apiTimeout));
        System.out.println("**********************Connected to the OpenAI API ****************");
        return openAiService;
    }

    public String sendMessageToGenerateText(OpenAiService openAiService, Map<String, String> messages) {
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(gptModel)
                .temperature(apiTemperature)
                .messages(Arrays.asList(new ChatMessage("system", messages.get("SYSTEM")), new ChatMessage("user", messages.get("USER"))))
                .build();

        StringBuilder builder = new StringBuilder();
        openAiService.createChatCompletion(chatCompletionRequest).getChoices().forEach(choice -> {
            builder.append(choice.getMessage().getContent());
        });
        return builder.toString();
    }

    public List<String> sendMessageToGenerateImages(OpenAiService openAiService, Map<String, String> messages) {
        CreateImageRequest chatCompletionRequest = CreateImageRequest.builder()
                .responseFormat(messages.get("response_format"))
                .prompt(messages.get("prompt"))
                .n(Integer.parseInt(messages.get("count")))
                .size(messages.get("image_size"))
                .build();

        return openAiService.createImage(chatCompletionRequest)
                .getData()
                .stream()
                .map(Image::getUrl).collect(Collectors.toList());
    }
}
