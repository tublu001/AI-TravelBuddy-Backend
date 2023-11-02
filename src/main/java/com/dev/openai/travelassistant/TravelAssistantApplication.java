package com.dev.openai.travelassistant;

import com.dev.openai.travelassistant.controller.v1.TravelAssistantBotController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TravelAssistantApplication {

	private static final Logger logger = LoggerFactory.getLogger(TravelAssistantBotController.class);
	public static void main(String[] args) {
		logger.info("Application Stareed");
		SpringApplication.run(TravelAssistantApplication.class, args);
	}

}
