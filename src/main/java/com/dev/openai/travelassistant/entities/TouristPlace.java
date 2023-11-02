package com.dev.openai.travelassistant.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TOURIST_PLACES")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TouristPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private int id;

    @Column(name = "PLACE")
    private String place;

    @Column(name = "CURRENCY")
    private String currency;

    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "TOTAL_DISTANCE")
    private String totalDistance;

    @Lob
    @Column(name = "TRANSPORTATION_DETAILS")
    private String transportationDetails;

    @Lob
    @Column(name = "CLIMATE_PREDICTION")
    private String climatePrediction;

    @Lob
    @Column(name = "FAMOUS_FOODS")
    private String famousFoods;

    @Lob
    @Column(name = "EVENTS")
    private String events;

    @Lob
    @Column(name = "CUSTOMER_REVIEWS")
    private String customerReviews;

    @Lob
    @Column(name = "NEAR_RESIDENCES")
    private String nearResidences;

    @Column(name = "SOURCE_CITY")
    private String sourceCity;

    @Column(name = "SOURCE_COUNTRY")
    private String sourceCountry;

    @Column(name = "DESTINATION_CITY")
    private String destinationCity;

    @Column(name = "DESTINATION_COUNTRY")
    private String destinationCountry;

    @Column(name = "STATE")
    private String state;

    @Column(name = "ZIP_CODE")
    private String zipCode;

    @Lob
    @Column(name = "IMAGE")
    private String imageUrl;

    @Column(name = "CREATED_AT")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
