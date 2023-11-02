package com.dev.openai.travelassistant.services;

import com.dev.openai.travelassistant.Utils.JsonUtils;
import com.dev.openai.travelassistant.config.OpenAIConfiguration;
import com.dev.openai.travelassistant.dao.TravelAssistantRepository;
import com.dev.openai.travelassistant.entities.TouristPlaces;
import com.dev.openai.travelassistant.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Service
public class TravelAssistantService {

    @Autowired
    private TravelAssistantRepository repository;

    @Value("${image.storage.path}")
    private String downloadDirectory;

    @Value("${image.format}")
    private String imageFormat;

    @Autowired
    private Environment environment;

    @Autowired
    private OpenAIConfiguration openAIService;

    @Autowired
    private OpenAiService openAiService;

    static final Map<String, String> imagePrompts = new HashMap<String, String>();

    @PostConstruct
    public void initializeImagePrompts() {
        imagePrompts.put("response_format", environment.getProperty("image.response.format"));
        imagePrompts.put("count", environment.getProperty("image.count"));
        imagePrompts.put("image_size", environment.getProperty("image.size"));
    }


    public TravelSearchResponse saveTouristPlaceDetails(String result, TravelSearchRequest travelSearchRequest) {
        TravelSearchResponse travelSearchResponse = new TravelSearchResponse();
        try {
            travelSearchResponse = handleErrorResponse(result);
            if (null != travelSearchResponse && !travelSearchResponse.getTouristPlaces().isEmpty()) {
                travelSearchResponse.getTouristPlaces().stream().forEach(touristPlace -> {
                    TouristPlaces _touristPlaces = new TouristPlaces();
                    String imagePrompt = String.format(environment.getProperty("image.prompt"), (touristPlace.getPlace() + " in " + travelSearchRequest.getDestinationCity() + ", " + travelSearchRequest.getDestinationCountry()));
                    System.out.println("Image Prompt: " + imagePrompt);
                    imagePrompts.put("prompt", imagePrompt);
//                    List<String> imageUrls = openAIService.sendMessageToGenerateImages(openAiService, imagePrompts);
//                    System.out.println("DALLE Image Urls: " + imageUrls);
                    try {
                        _touristPlaces.setClimatePrediction(JsonUtils.toJson(touristPlace.getClimatePrediction()));
                        _touristPlaces.setTransportationDetails(JsonUtils.toJson(touristPlace.getTransportationDetails()));
                        _touristPlaces.setDescription(touristPlace.getDescription());
                        _touristPlaces.setPlace(touristPlace.getPlace());
                        _touristPlaces.setCategory(touristPlace.getCategory());
                        _touristPlaces.setCurrency(touristPlace.getCurrency());
                        _touristPlaces.setFamousFoods(JsonUtils.toJson(touristPlace.getFamousFoods()));
                        _touristPlaces.setEvents(JsonUtils.toJson(touristPlace.getEvents()));
                        _touristPlaces.setCustomerReviews(JsonUtils.toJson(touristPlace.getCustomerReviews()));
                        _touristPlaces.setNearResidences(JsonUtils.toJson(touristPlace.getResidenceDetails()));
//                        _touristPlace.setImageUrl(JsonUtils.toJson(saveImages(imageUrls, touristPlace.getPlace())));
                        _touristPlaces.setTotalDistance(touristPlace.getTotalDistance());
                        _touristPlaces.setAgents(JsonUtils.toJson(touristPlace.getTravelAgents()));
                        Optional.ofNullable(touristPlace.getLocationDetails()).ifPresent(locationDetails -> {
                                    _touristPlaces.setState(locationDetails.getState());
                                    _touristPlaces.setZipCode(locationDetails.getZipcode());
                                    _touristPlaces.setDestinationCity(travelSearchRequest.getDestinationCity());
                                    _touristPlaces.setDestinationCountry(travelSearchRequest.getDestinationCountry());
                                }
                        );
                        _touristPlaces.setSourceCity(travelSearchRequest.getSourceCity());
                        _touristPlaces.setSourceCountry(travelSearchRequest.getSourceCountry());
                        repository.save(_touristPlaces);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return travelSearchResponse;
    }

    public List<String> saveImages(List<String> imageUrls, String place) throws MalformedURLException {
        List<String> savedImagesUrls = new ArrayList<>();

        for (int i = 0; i < imageUrls.size(); i++) {
            String imageUrl = imageUrls.get(i);
            String fileName = place + "_" + (i + 1) + "." + imageFormat;
            System.out.println("Image Name: " + fileName);
            URL url = new URL(imageUrl);
            String filePath = downloadDirectory + fileName;
            try (InputStream inputStream = url.openStream();
                 OutputStream outputStream = new FileOutputStream(filePath)) {

                // Read and write the image data
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                savedImagesUrls.add(filePath);
                System.out.println("Downloaded: " + fileName);
                Thread.sleep(Long.parseLong(environment.getProperty("image.download.thread.sleep")));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Saved Images: " + savedImagesUrls);
        return savedImagesUrls;
    }

    public long getCountByDestinationCityAndSourceCity(String destinationCity, String sourceCity) {
        return repository.countByDestinationCityAndSourceCity(destinationCity, sourceCity);
    }

    public TravelSearchResponse getTouristPlaceDetails(String sourceCity, String destinationCity) {
        List<TouristPlaces> touristPlaces = repository.findBySourceCityAndDestinationCity(sourceCity, destinationCity);
        TravelSearchResponse travelSearchResponse = new TravelSearchResponse();
        List<TouristPlace> touristPlaceList = new ArrayList<>();
        touristPlaces.stream().forEach(touristPlace -> {
            TouristPlace _tourist = new TouristPlace();
            try {
                _tourist.setClimatePrediction(JsonUtils.fromJson(touristPlace.getClimatePrediction(), ClimatePrediction.class));
                _tourist.setCurrency(touristPlace.getCurrency());
                _tourist.setCategory(touristPlace.getCategory());
                _tourist.setCustomerReviews(JsonUtils.fromJson(touristPlace.getCustomerReviews(), new TypeReference<List<CustomerReview>>() {
                }));
                _tourist.setDescription(touristPlace.getDescription());
                _tourist.setEvents(JsonUtils.fromJson(touristPlace.getEvents(), new TypeReference<List<Event>>() {
                }));
                _tourist.setFamousFoods(JsonUtils.fromJson(touristPlace.getFamousFoods(), new TypeReference<List<String>>() {
                }));
                LocationDetails locationDetails = new LocationDetails();
                locationDetails.setCity(touristPlace.getDestinationCity());
                locationDetails.setCountry(touristPlace.getDestinationCountry());
                locationDetails.setState(touristPlace.getState());
                locationDetails.setZipcode(touristPlace.getZipCode());
                _tourist.setLocationDetails(locationDetails);
                _tourist.setPlace(touristPlace.getPlace());
                _tourist.setResidenceDetails(JsonUtils.fromJson(touristPlace.getNearResidences(), new TypeReference<List<ResidenceDetail>>() {
                }));
                _tourist.setTotalDistance(touristPlace.getTotalDistance());
                _tourist.setTransportationDetails(JsonUtils.fromJson(touristPlace.getTransportationDetails(), new TypeReference<List<TransportationDetail>>() {
                }));
                _tourist.setTravelAgents(JsonUtils.fromJson(touristPlace.getAgents(), new TypeReference<List<TravelAgent>>() {
                }));
                _tourist.setTotalDistance(touristPlace.getTotalDistance());
                touristPlaceList.add(_tourist);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        travelSearchResponse.setTouristPlaces(touristPlaceList);
        return travelSearchResponse;
    }


    private TravelSearchResponse handleErrorResponse(String result) throws JsonProcessingException {
        try {
            return JsonUtils.fromJson(result, TravelSearchResponse.class);
        } catch (JsonProcessingException e) {
            System.out.println("result: " + result);
            System.out.println("ERROR: " + e.getMessage());
            return null;
//            return generaTravelSearchResponse(result);
        }
    }

    public static void main(String[] args) {
        try {
            // String json = "{\"touristPlaces\":[{\"place\":\"The Beatles Story\",\"currency\":null,\"description\":\"A museum about the famous band 'The Beatles' with exhibits and historical memorabilia\",\"transportationDetails\":[{\"mode\":\"Flight\",\"duration\":\"15 hours 30 minutes\",\"distance\":\"8,394 km\",\"cost\":\"1500 USD\"},{\"mode\":\"Train\",\"duration\":\"7 hours\",\"distance\":\"350 km\",\"cost\":\"75 USD\"}],\"climatePrediction\":{\"temperature\":\"10\\u00b0C - 15\\u00b0C\",\"weather\":\"Rainy\"},\"famousFoods\":[\"Scouse\",\"Liver Pate\",\"Lancashire Hotpot\"],\"events\":[{\"event\":\"The Liverpool International Music Festival\",\"timeline\":\"Late July\"},{\"event\":\"Liverpool Food & Drink Festival\",\"timeline\":\"Late September\"}],\"customerReviews\":[{\"name\":\"John\",\"review\":\"It was an amazing experience at The Beatles Story museum. I highly recommend it.\",\"rating\":\"5 out of 5\"},{\"name\":\"Lucy\",\"review\":\"The exhibits were great but slightly overpriced.\",\"rating\":\"4 out of 5\"}],\"residenceDetails\":[{\"name\":\"The Shankly Hotel\",\"plans\":[\"Bed and Breakfast\",\"Standard Room\",\"Deluxe Suite\"],\"address\":\"60 Victoria St, Liverpool L1 6JD, United Kingdom\"},{\"name\":\"Jurys Inn Liverpool\",\"plans\":[\"Room Only\",\"Standard Room\",\"Executive Suite\"],\"address\":\"31 Keel Wharf, Liverpool L3 4FN, United Kingdom\"}],\"travelAgents\":[{\"name\":\"Expedia\",\"contactNo\":\"+44 0203 788 0445\",\"website\":\"https:\\/\\/www.expedia.com\\/\",\"emailId\":\"customerservice@expedia.com\",\"address\":\"27 Ropemaker St, London EC2Y 9AR, United Kingdom\"},{\"name\":\"Thomas Cook\",\"contactNo\":\"+44 0843 104 0500\",\"website\":\"https:\\/\\/www.thomascook.com\\/\",\"emailId\":\"customer.service@thomascook.com\",\"address\":\"Westpoint, Peterborough Business Park, Lynch Wood, Peterborough PE2 6FZ, United Kingdom\"}],\"locationDetails\":null,\"totalDistance\":null},{\"place\":\"Liverpool Cathedral\",\"currency\":null,\"description\":\"The largest Cathedral in UK dedicated as a tribute to World War One and exhibits various artifacts, unusual artefacts, and treasures.\",\"transportationDetails\":[{\"mode\":\"Flight\",\"duration\":\"15 hours 30 minutes\",\"distance\":\"8,394 km\",\"cost\":\"1500 USD\"},{\"mode\":\"Train\",\"duration\":\"7 hours\",\"distance\":\"350 km\",\"cost\":\"75 USD\"}],\"climatePrediction\":{\"temperature\":\"15\\u00b0C - 22\\u00b0C\",\"weather\":\"Sunny\"},\"famousFoods\":[\"Toxteth Chicken\",\"Crispy Scallops\",\"Liverpool cheese\"],\"events\":[{\"event\":\"International Mural Festival\",\"timeline\":\"May\"},{\"event\":\"Liverpool Summer Pops concert series\",\"timeline\":\"June-July\"}],\"customerReviews\":[{\"name\":\"Sarah\",\"review\":\"The Liverpool Cathedral is an absolute masterpiece! A must-visit for anyone travelling to Liverpool.\",\"rating\":\"5 out of 5\"},{\"name\":\"David\",\"review\":\"The Cathedral is beautiful but could use better maintenance.\",\"rating\":\"4 out of 5\"}],\"residenceDetails\":[{\"name\":\"The Nadler Liverpool\",\"plans\":[\"Bed and Breakfast\",\"Standard Room\",\"Deluxe Suite\"],\"address\":\"29 Seel St, Liverpool L1 4AU, United Kingdom\"},{\"name\":\"Hilton Liverpool City Centre\",\"plans\":[\"Room Only\",\"Standard Room\",\"Executive Suite\"],\"address\":\"3 Thomas Steers Way, Liverpool L1 8LW, United Kingdom\"}],\"travelAgents\":[{\"name\":\"MakeMyTrip\",\"contactNo\":\"+44 0808 189 2608\",\"website\":\"https:\\/\\/www.makemytrip.com\\/\",\"emailId\":\"customerservice@makemytrip.com\",\"address\":\"St James's Square, London SW1Y 4JH, United Kingdom\"},{\"name\":\"Yatra\",\"contactNo\":\"+44 0800 121 4780\",\"website\":\"https:\\/\\/www.yatra.com\\/\",\"emailId\":\"support@yatra.com\",\"address\":\"Riverside Way, Uxbridge UB8 2YF, United Kingdom\"}],\"locationDetails\":null,\"totalDistance\":null}]}";
            String json = "{\"touristPlaces\":[{\"place\":\"The Beatles Story\",\"currency\":null,\"description\":\"A museum about the famous band 'The Beatles' with exhibits and historical memorabilia\",\"transportationDetails\":[{\"mode\":\"Flight\",\"duration\":\"15 hours 30 minutes\",\"distance\":\"8,394 km\",\"cost\":\"1500 USD\",},{\"mode\":\"Train\",\"duration\":\"7 hours\",\"distance\":\"350 km\",\"cost\":{\"amount\":\"50\",\"currency\":\"USD\"}}],\"climatePrediction\":{\"temperature\":\"10\\u00b0C - 15\\u00b0C\",\"weather\":\"Rainy\"},\"famousFoods\":[\"Scouse\",\"Liver Pate\",\"Lancashire Hotpot\"],\"events\":[{\"event\":\"The Liverpool International Music Festival\",\"timeline\":\"Late July\"},{\"event\":\"Liverpool Food & Drink Festival\",\"timeline\":\"Late September\"}],\"customerReviews\":[{\"name\":\"John\",\"review\":\"It was an amazing experience at The Beatles Story museum. I highly recommend it.\",\"rating\":\"5 out of 5\"},{\"name\":\"Lucy\",\"review\":\"The exhibits were great but slightly overpriced.\",\"rating\":\"4 out of 5\"}],\"residenceDetails\":[{\"name\":\"The Shankly Hotel\",\"plans\":[\"Bed and Breakfast\",\"Standard Room\",\"Deluxe Suite\"],\"address\":\"60 Victoria St, Liverpool L1 6JD, United Kingdom\"},{\"name\":\"Jurys Inn Liverpool\",\"plans\":[\"Room Only\",\"Standard Room\",\"Executive Suite\"],\"address\":\"31 Keel Wharf, Liverpool L3 4FN, United Kingdom\"}],\"travelAgents\":[{\"name\":\"Expedia\",\"contactNo\":\"+44 0203 788 0445\",\"website\":\"https:\\/\\/www.expedia.com\\/\",\"emailId\":\"customerservice@expedia.com\",\"address\":\"27 Ropemaker St, London EC2Y 9AR, United Kingdom\"},{\"name\":\"Thomas Cook\",\"contactNo\":\"+44 0843 104 0500\",\"website\":\"https:\\/\\/www.thomascook.com\\/\",\"emailId\":\"customer.service@thomascook.com\",\"address\":\"Westpoint, Peterborough Business Park, Lynch Wood, Peterborough PE2 6FZ, United Kingdom\"}],\"locationDetails\":null,\"totalDistance\":null},{\"place\":\"Liverpool Cathedral\",\"currency\":null,\"description\":\"The largest Cathedral in UK dedicated as a tribute to World War One and exhibits various artifacts, unusual artefacts, and treasures.\",\"transportationDetails\":[{\"mode\":\"Flight\",\"duration\":\"15 hours 30 minutes\",\"distance\":\"8,394 km\",\"cost\":\"1500 USD\"},{\"mode\":\"Train\",\"duration\":\"7 hours\",\"distance\":\"350 km\",\"cost\":\"75 USD\"}],\"climatePrediction\":{\"temperature\":\"15\\u00b0C - 22\\u00b0C\",\"weather\":\"Sunny\"},\"famousFoods\":[\"Toxteth Chicken\",\"Crispy Scallops\",\"Liverpool cheese\"],\"events\":[{\"event\":\"International Mural Festival\",\"timeline\":\"May\"},{\"event\":\"Liverpool Summer Pops concert series\",\"timeline\":\"June-July\"}],\"customerReviews\":[{\"name\":\"Sarah\",\"review\":\"The Liverpool Cathedral is an absolute masterpiece! A must-visit for anyone travelling to Liverpool.\",\"rating\":\"5 out of 5\"},{\"name\":\"David\",\"review\":\"The Cathedral is beautiful but could use better maintenance.\",\"rating\":\"4 out of 5\"}],\"residenceDetails\":[{\"name\":\"The Nadler Liverpool\",\"plans\":[\"Bed and Breakfast\",\"Standard Room\",\"Deluxe Suite\"],\"address\":\"29 Seel St, Liverpool L1 4AU, United Kingdom\"},{\"name\":\"Hilton Liverpool City Centre\",\"plans\":[\"Room Only\",\"Standard Room\",\"Executive Suite\"],\"address\":\"3 Thomas Steers Way, Liverpool L1 8LW, United Kingdom\"}],\"travelAgents\":[{\"name\":\"MakeMyTrip\",\"contactNo\":\"+44 0808 189 2608\",\"website\":\"https:\\/\\/www.makemytrip.com\\/\",\"emailId\":\"customerservice@makemytrip.com\",\"address\":\"St James's Square, London SW1Y 4JH, United Kingdom\"},{\"name\":\"Yatra\",\"contactNo\":\"+44 0800 121 4780\",\"website\":\"https:\\/\\/www.yatra.com\\/\",\"emailId\":\"support@yatra.com\",\"address\":\"Riverside Way, Uxbridge UB8 2YF, United Kingdom\"}],\"locationDetails\":null,\"totalDistance\":null}]}";
            TravelSearchResponse generaTravelSearchResponse = generaTravelSearchResponse(json);
            System.out.println(generaTravelSearchResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TravelSearchResponse generaTravelSearchResponse(String json) {
        TravelSearchResponse response = new TravelSearchResponse();
        try {
            JSONObject jsonResponse = new JSONObject(removeExtraCommas(json));
            response = new TravelSearchResponse();
            if (checkKeys(jsonResponse, "touristPlaces", false)) {
                JSONArray places = jsonResponse.getJSONArray("touristPlaces");
                List<TouristPlace> touristPlaces = new ArrayList<>(places.length());
                for (int i = 0; i < places.length(); i++) {
                    JSONObject touristPlaceObject = places.getJSONObject(i);
                    TouristPlace touristPlace = new TouristPlace();
                    touristPlace.setPlace(checkKeys(touristPlaceObject,"place", true) ? touristPlaceObject.getString("place") : null);
                    touristPlace.setCategory(checkKeys(touristPlaceObject, "category", true) ? touristPlaceObject.getString("category") : null);
                    touristPlace.setDescription(checkKeys(touristPlaceObject, "description", true) ? touristPlaceObject.getString("description") : null);
                    touristPlace.setCurrency(checkKeys(touristPlaceObject,"currency" , true) ? touristPlaceObject.getString("currency") : null);
                    touristPlace.setTotalDistance(checkKeys(touristPlaceObject,"totalDistance", true) ? touristPlaceObject.getString("totalDistance") : null);
                    if (checkKeys(touristPlaceObject,"transportationDetails", true)) {
                        JSONArray transportationDetailsArray = touristPlaceObject.getJSONArray("transportationDetails");
                        List<TransportationDetail> transportationDetails = new ArrayList<>(transportationDetailsArray.length());
                        for (int j = 0; j < transportationDetailsArray.length(); j++) {
                            JSONObject transportationDetailObject = transportationDetailsArray.getJSONObject(j);
                            TransportationDetail transportationDetail = new TransportationDetail();
                            transportationDetail.setMode(checkKeys(transportationDetailObject,"mode", true) ? transportationDetailObject.getString("mode") : null);
                            transportationDetail.setDuration(checkKeys(transportationDetailObject,"duration", true) ? transportationDetailObject.getString("duration") : null);
                            transportationDetail.setDistance(checkKeys(transportationDetailObject,"distance", true) ? transportationDetailObject.getString("distance") : null);
                            transportationDetail.setCost(checkKeys(transportationDetailObject,"cost", true)? transportationDetailObject.getString("cost") : null);
                            transportationDetails.add(transportationDetail);
                        }
                        touristPlace.setTransportationDetails(transportationDetails);
                    }
                    if (checkKeys(touristPlaceObject,"climatePrediction", false)) {
                        JSONObject climatePredictionObject = touristPlaceObject.getJSONObject("climatePrediction");
                        ClimatePrediction climatePrediction = new ClimatePrediction();
                        climatePrediction.setTemperature(checkKeys(climatePredictionObject,"temperature", true) ? climatePredictionObject.getString("temperature") : null);
                        touristPlace.setClimatePrediction(climatePrediction);
                    }
                    JSONArray famousFoodsObject = touristPlaceObject.getJSONArray("famousFoods");
                    List<String> foamousFoods = new ArrayList<>(famousFoodsObject.length());
                    for (int k = 0; k < famousFoodsObject.length(); k++) {
                        foamousFoods.add(famousFoodsObject.getString(k));
                    }
                    touristPlace.setFamousFoods(foamousFoods);
                    if (checkKeys(touristPlaceObject,"events", false)) {
                        JSONArray eventsArray = touristPlaceObject.getJSONArray("events");
                        List<Event> events = new ArrayList<>(eventsArray.length());
                        for (int l = 0; l < eventsArray.length(); l++) {
                            JSONObject eventDetailObject = eventsArray.getJSONObject(l);
                            Event event = new Event();
                            event.setEvent(checkKeys(eventDetailObject,"temperature", true) ? eventDetailObject.getString("event") : null);
                            event.setTimeline(checkKeys(eventDetailObject,"timeline", true) ? eventDetailObject.getString("timeline") : null);
                            events.add(event);
                        }
                        touristPlace.setEvents(events);
                    }
                    if (checkKeys(touristPlaceObject,"customerReviews", false)) {
                        JSONArray customerReviewArray = touristPlaceObject.getJSONArray("customerReviews");
                        List<CustomerReview> customerReviews = new ArrayList<>(customerReviewArray.length());
                        for (int m = 0; m < customerReviewArray.length(); m++) {
                            JSONObject customerReviewObject = customerReviewArray.getJSONObject(m);
                            CustomerReview customerReview = new CustomerReview();
                            customerReview.setName(checkKeys(customerReviewObject,"name", true)? customerReviewObject.getString("name") : null);
                            customerReview.setRating(checkKeys(customerReviewObject,"rating", true) ? customerReviewObject.getString("rating") : null);
                            customerReview.setReview(checkKeys(customerReviewObject,"review", true)? customerReviewObject.getString("review") : null);
                            customerReviews.add(customerReview);
                        }
                        touristPlace.setCustomerReviews(customerReviews);
                    }
                    if (checkKeys(touristPlaceObject,"residenceDetails", false)) {
                        JSONArray residenceDetailsArray = touristPlaceObject.getJSONArray("residenceDetails");
                        List<ResidenceDetail> residenceDetails = new ArrayList<>(residenceDetailsArray.length());
                        for (int n = 0; n < residenceDetailsArray.length(); n++) {
                            JSONObject residenceDetailObject = residenceDetailsArray.getJSONObject(n);
                            ResidenceDetail residenceDetail = new ResidenceDetail();
                            residenceDetail.setName(checkKeys(residenceDetailObject,"name", true) ? residenceDetailObject.getString("name") : null);
                            residenceDetail.setAddress(checkKeys(residenceDetailObject,"address", true) ? residenceDetailObject.getString("address") : null);
                            if (checkKeys(residenceDetailObject,"plans", false)) {
                                JSONArray plansObject = residenceDetailObject.getJSONArray("plans");
                                List<String> plans = new ArrayList<>(plansObject.length());
                                for (int k = 0; k < plansObject.length(); k++) {
                                    plans.add(plansObject.getString(k));
                                }
                                residenceDetail.setPlans(plans);
                            }
                            residenceDetails.add(residenceDetail);
                        }
                        touristPlace.setResidenceDetails(residenceDetails);
                    }
                    if (checkKeys(touristPlaceObject,"travelAgents", false)) {
                        JSONArray travelAgentsArray = touristPlaceObject.getJSONArray("travelAgents");
                        List<TravelAgent> travelAgents = new ArrayList<>(travelAgentsArray.length());
                        for (int o = 0; o < travelAgentsArray.length(); o++) {
                            JSONObject travelAgentObject = travelAgentsArray.getJSONObject(o);
                            TravelAgent travelAgent = new TravelAgent();
                            travelAgent.setAddress(checkKeys(travelAgentObject,"address", true) ? travelAgentObject.getString("address") : null);
                            travelAgent.setContactNo(checkKeys(travelAgentObject,"contactNo", true) ? travelAgentObject.getString("contactNo") : null);
                            travelAgent.setEmailId(checkKeys(travelAgentObject,"emailId", true) ? travelAgentObject.getString("emailId") : null);
                            travelAgent.setName(checkKeys(travelAgentObject,"name", true) ? travelAgentObject.getString("name") : null);
                            travelAgent.setWebsite(checkKeys(travelAgentObject,"website", true)  ? travelAgentObject.getString("website") : null);
                            travelAgents.add(travelAgent);
                        }
                        touristPlace.setTravelAgents(travelAgents);
                    }
                    touristPlaces.add(touristPlace);
                }
                response.setTouristPlaces(touristPlaces);
            }
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
        return response;
    }

    private static boolean checkKeys(JSONObject jsonObject, String key, boolean isString) {
        return jsonObject.has(key) && !jsonObject.isNull(key) && (isString ? !(jsonObject.get(key) instanceof JSONObject) : true);
    }

    private static String removeExtraCommas(String json){
        try {
            // Create an ObjectMapper
            ObjectMapper mapper = new ObjectMapper();

            // Parse the JSON string into a JsonNode
            JsonNode jsonNode = mapper.readTree(json);

            // Convert the JsonNode back to a JSON string
            String updatedJsonString = mapper.writeValueAsString(jsonNode);

            // Print the updated JSON string
            System.out.println(updatedJsonString);
            return updatedJsonString;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
