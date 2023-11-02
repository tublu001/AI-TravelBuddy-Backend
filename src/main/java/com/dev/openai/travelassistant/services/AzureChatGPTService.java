package com.dev.openai.travelassistant.services;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.azure.core.util.Configuration.*;

@SuppressWarnings("ALL")
@Service
public class AzureChatGPTService {

    private static OpenAIClient client;

    @Value("${azure.openai.key}")
    private String apiKey;

    @Value("${azure.openai.endpoint}")
    private String endpoint;

    @Value("${azure.openai.deploymentOrModelId}")
    private String deploymentOrModelId = "gpt-3.5-turbo";

    @PostConstruct
    public void initializeAzureGPTService() {
        ClientOptions clientOptions = new ClientOptions();
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("max_tokens", "3000"));
        headers.add(new Header("temperature", "0.7"));
        headers.add(new Header("frequency_penalty", "0"));
        headers.add(new Header("presence_penalty", "0"));
        headers.add(new Header("top_p", "0.95"));
        headers.add(new Header("stop", ""));
        clientOptions.setHeaders(headers);

        Configuration configuration = new Configuration();
        configuration.put(PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT, "240000");
        configuration.put(PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT, "240000");
        configuration.put(PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT, "240000");
        configuration.put(PROPERTY_AZURE_REQUEST_READ_TIMEOUT, "240000");
        client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .configuration(configuration)
                .serviceVersion(OpenAIServiceVersion.V2023_07_01_PREVIEW)
                .clientOptions(clientOptions)
                .credential(new AzureKeyCredential(apiKey))
                .buildClient();
        System.out.println("**********************Connected to the Azure Chat API ****************");
    }


    public String sendMessageToGenerateText(Map<String, String> prompts) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(ChatRole.SYSTEM, prompts.get("system")));
        chatMessages.add(new ChatMessage(ChatRole.USER, prompts.get("user")));
        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, new ChatCompletionsOptions(chatMessages));
        return chatCompletions.getChoices().get(0).getMessage().getContent();
    }
}