package com.dev.openai.travelassistant.config;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.azure.core.util.Configuration.*;

@SuppressWarnings("ALL")
@org.springframework.context.annotation.Configuration
public class AzureAIConfiguration {


    @Value("${azure.openai.key}")
    private String apiKey;

    @Value("${azure.openai.endpoint}")
    private String endpoint;

    @Value("${azure.openai.deploymentOrModelId}")
    private String deploymentOrModelId = "gpt-3.5-turbo";

    @Value("${azure.openai.stop}")
    private String stop = "";

    @Autowired
    private Environment environment;

    @Bean
    public OpenAIClient initializeAzureGPTService() {
        ClientOptions clientOptions = new ClientOptions();
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("max_tokens", environment.getProperty("azure.openai.max.tokens")));
        headers.add(new Header("temperature", environment.getProperty("azure.openai.temperature")));
        headers.add(new Header("frequency_penalty", environment.getProperty("azure.openai.frequency.penality")));
        headers.add(new Header("presence_penalty", environment.getProperty("azure.openai.presence.penality")));
        headers.add(new Header("top_p", environment.getProperty("azure.openai.top_p")));
        headers.add(new Header("stop", stop));
        clientOptions.setHeaders(headers);

        Configuration configuration = new Configuration();
        configuration.put(PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT, environment.getProperty("azure.openai.request.connect.timeout"));
        configuration.put(PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT, environment.getProperty("azure.openai.request.write.timeout"));
        configuration.put(PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT, environment.getProperty("azure.openai.request.response.timeout"));
        configuration.put(PROPERTY_AZURE_REQUEST_READ_TIMEOUT, environment.getProperty("azure.openai.request.read.timeout"));
        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .configuration(configuration)
                .serviceVersion(OpenAIServiceVersion.V2023_07_01_PREVIEW)
                .clientOptions(clientOptions)
                .credential(new AzureKeyCredential(apiKey))
                .buildClient();
        System.out.println("**********************Connected to the Azure Chat API ****************");
        return client;
    }

    public String sendMessageToGenerateText(OpenAIClient client, Map<String, String> prompts) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(ChatRole.SYSTEM, prompts.get("system")));
        chatMessages.add(new ChatMessage(ChatRole.USER, prompts.get("user")));
        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, new ChatCompletionsOptions(chatMessages));
        return chatCompletions.getChoices().get(0).getMessage().getContent();
    }
}