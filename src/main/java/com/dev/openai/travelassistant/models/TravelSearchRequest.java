package com.dev.openai.travelassistant.models;

import lombok.Data;

import java.io.Serializable;

@Data
public class TravelSearchRequest implements Serializable {

    private String destinationCity;
    private String destinationCountry;
    private String sourceCity;
    private String sourceCountry;
}
