package com.dev.openai.travelassistant.services;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class OpenAIService {

    private static OpenAiService openAiService;

    @Value("${openai.key}")
    private String apiKey;

    @Value("${openai.timeout}")
    private int apiTimeout;

    @Value("${openai.model}")
    private String gptModel = "gpt-3.5-turbo";

    @PostConstruct
    public void initGptService() {
        openAiService = new OpenAiService(apiKey,
                Duration.ofSeconds(apiTimeout));
        System.out.println("**********************Connected to the OpenAI API ****************");
    }

    public String sendMessageToGenerateText(Map<String, String> messages) {
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(gptModel)
                .temperature(0.8)
                .messages(Arrays.asList(new ChatMessage("system", messages.get("SYSTEM")), new ChatMessage("user", messages.get("USER"))))
                .build();

        StringBuilder builder = new StringBuilder();
        openAiService.createChatCompletion(chatCompletionRequest).getChoices().forEach(choice -> {
            builder.append(choice.getMessage().getContent());
        });
        return builder.toString();
    }

    public List<String> sendMessageToGenerateImages(Map<String, String> messages) {
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
