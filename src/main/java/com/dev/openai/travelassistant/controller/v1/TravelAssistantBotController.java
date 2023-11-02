package com.dev.openai.travelassistant.controller.v1;

import com.azure.ai.openai.OpenAIClient;
import com.dev.openai.travelassistant.Utils.JsonUtils;
import com.dev.openai.travelassistant.config.AzureAIConfiguration;
import com.dev.openai.travelassistant.dao.InMemoryDB;
import com.dev.openai.travelassistant.config.OpenAIConfiguration;
import com.dev.openai.travelassistant.models.TravelSearchRequest;
import com.dev.openai.travelassistant.models.TravelSearchResponse;
import com.dev.openai.travelassistant.services.TravelAssistantService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class TravelAssistantBotController {

    private final Logger logger = LoggerFactory.getLogger(TravelAssistantBotController.class);

    @Autowired
    private OpenAIConfiguration openAIService;

    @Autowired
    private AzureAIConfiguration getChatCompletionsService;

    @Autowired
    private Environment environment;

    @Autowired
    private JsonUtils jsonUtils;

    @Autowired
    private TravelAssistantService travelAssistantService;

    @Autowired
    private OpenAIClient client;

    @PostMapping("/planYourTour/json")
    public TravelSearchResponse planYourTourInJson(@RequestBody TravelSearchRequest travelSearchRequest) throws JsonProcessingException {
        long startTime = System.currentTimeMillis();

        TravelSearchResponse _travelSearchResponse = travelAssistantService.getTouristPlaceDetails(travelSearchRequest.getSourceCity(), travelSearchRequest.getDestinationCity());
        if (null != _travelSearchResponse && !_travelSearchResponse.getTouristPlaces().isEmpty()) {
            return _travelSearchResponse;
        }
        String source = travelSearchRequest.getSourceCity() + ", " + travelSearchRequest.getSourceCountry();
        String destination = travelSearchRequest.getDestinationCity() + ", " + travelSearchRequest.getDestinationCountry();
        String userPrompt = String.format("I want to travel from source location  %s to  destination location %s", source, destination);
        Map<String, String> prompts = new HashMap<String, String>() {
            {
                put("system", environment.getProperty("prompt.travels.details"));
                put("user", userPrompt);
            }
        };
        Map<String, String> imagePrompts = new HashMap<String, String>() {
            {
                put("response_format", environment.getProperty("image.format"));
                put("count", environment.getProperty("image.count"));
                put("image_size", environment.getProperty("image.size"));
            }
        };
        String result = getChatCompletionsService.sendMessageToGenerateText(client, prompts);
        System.out.println("Response: " + result);
        TravelSearchResponse travelSearchResponse = travelAssistantService.saveTouristPlaceDetails(result, travelSearchRequest);
        System.out.println("Total Time Taken: " + ((System.currentTimeMillis() - startTime) / 1000 * 60) + "secs");
        return travelSearchResponse;
    }

    @PostMapping("/planYourTour/text")
    public String planYourTourInText(@RequestBody TravelSearchRequest travelSearchRequest) throws JsonProcessingException {
        long startTime = System.currentTimeMillis();
        String source = travelSearchRequest.getSourceCity() + ", " + travelSearchRequest.getSourceCountry();
        String destination = travelSearchRequest.getDestinationCity() + ", " + travelSearchRequest.getDestinationCountry();
        String userPrompt = String.format("I want to travel from source location  %s to  destination location %s", source, destination);
        Map<String, String> prompts = new HashMap<String, String>() {
            {
                put("system", environment.getProperty("prompt.travels.details"));
                put("user", userPrompt);
            }
        };
        String result = getChatCompletionsService.sendMessageToGenerateText(client, prompts);

        System.out.println("Total Time Taken: " + ((System.currentTimeMillis() - startTime) / 1000 * 60) + "secs");
        return result;
    }
}
