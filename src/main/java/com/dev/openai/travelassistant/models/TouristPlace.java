package com.dev.openai.travelassistant.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TouristPlace implements Serializable {
    private String place;
    private String currency;
    private String category;
    private String description;
    private List<TransportationDetail> transportationDetails;
    private ClimatePrediction climatePrediction;
    private List<String> famousFoods;
    private List<Event> events;
    private List<CustomerReview> customerReviews;
    private List<ResidenceDetail> residenceDetails;
    private List<TravelAgent> travelAgents;
    private LocationDetails locationDetails;
    private String totalDistance;
}
