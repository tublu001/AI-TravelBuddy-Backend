package com.dev.openai.travelassistant.dao;

import com.dev.openai.travelassistant.entities.TouristPlaces;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelAssistantRepository extends JpaRepository<TouristPlaces, Integer> {

    long countByDestinationCityAndSourceCity(String sourceCity, String destinationCity);

    List<TouristPlaces> findBySourceCityAndDestinationCity(String sourceCity, String destinationCity);
}
