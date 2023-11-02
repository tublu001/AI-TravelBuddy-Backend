package com.dev.openai.travelassistant.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TravelAgent implements Serializable {
    private String name;
    private String contactNo;
    private String website;
    private String emailId;
    private String address;
}
