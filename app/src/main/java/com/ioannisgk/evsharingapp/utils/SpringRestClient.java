package com.ioannisgk.evsharingapp.utils;

import android.util.Base64;

import com.ioannisgk.evsharingapp.entities.Station;
import com.ioannisgk.evsharingapp.entities.User;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class SpringRestClient {

    public static String REST_SERVICE_URI = "http://" + Global.ipAddress + ":" + Global.portNumber + "/evsharing-platform3/api";
    public static String AUTH_SERVER_URI = "http://" + Global.ipAddress + ":" + Global.portNumber + "/evsharing-platform3/oauth/token";

    // Enable for localhost connection only
    // public static String REST_SERVICE_URI = "http://10.0.2.2:" + Global.portNumber + "/evsharing-platform3/api";
    // public static String AUTH_SERVER_URI = "http://10.0.2.2:" + Global.portNumber + "/evsharing-platform3/oauth/token";
    
    public static final String QPM_PASSWORD_GRANT = "?grant_type=password&username=evsharingUser&password=evsharingPass";
    public static final String QPM_ACCESS_TOKEN = "?access_token=";

    // Class constructor
    public SpringRestClient() {

    }

    // Prepare HTTP headers

    private static HttpHeaders getHeaders(){

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return headers;
    }

    // Add HTTP authorization header using Basic Authentication to send client credentials

    private static HttpHeaders getHeadersWithClientCredentials(){

        String plainClientCredentials="evsharingClient:evsharingPass";
        String base64ClientCredentials = Base64.encodeToString(plainClientCredentials.getBytes(), Base64.DEFAULT);

        HttpHeaders headers = getHeaders();
        headers.add("Authorization", "Basic " + base64ClientCredentials);
        return headers;
    }

    // Send a POST request to get an access token which will be send with each request

    @SuppressWarnings({ "unchecked"})
    public AuthTokenInfo sendTokenRequest(){

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        HttpEntity<String> request = new HttpEntity<String>(getHeadersWithClientCredentials());
        ResponseEntity<Object> response = restTemplate.exchange(
                AUTH_SERVER_URI + QPM_PASSWORD_GRANT,
                HttpMethod.POST,
                request,
                Object.class);

        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)response.getBody();
        AuthTokenInfo tokenInfo = null;

        if(map != null){

            tokenInfo = new AuthTokenInfo();
            tokenInfo.setAccess_token((String)map.get("access_token"));
            tokenInfo.setToken_type((String)map.get("token_type"));
            tokenInfo.setRefresh_token((String)map.get("refresh_token"));
            tokenInfo.setExpires_in((int)map.get("expires_in"));
            tokenInfo.setScope((String)map.get("scope"));

        } else {
            System.out.println("No user exists");
        }
        return tokenInfo;
    }

    // Send a POST request to login a user

    public User loginUser(AuthTokenInfo tokenInfo, String theUsername, String thePassword) {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        // Create the request body as a hash map to store the username and password

        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        body.put("username", theUsername);
        body.put("password", thePassword);

        HttpEntity<Object> request = new HttpEntity<Object>(body, getHeaders());
        ResponseEntity<User> response = restTemplate.exchange(
                REST_SERVICE_URI + "/login/" + QPM_ACCESS_TOKEN + tokenInfo.getAccess_token(),
                HttpMethod.POST,
                request,
                User.class);

        // Return user object

        if (response.getBody() != null) {
            return response.getBody();
        } else return new User("Invalid login details");
    }

    // Send a POST request to create a new user

    public Boolean createUser(AuthTokenInfo tokenInfo, User theUser) {
        URI uri = null;

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        HttpEntity<Object> request = new HttpEntity<Object>(theUser, getHeaders());

        try {
            uri = restTemplate.postForLocation(
                    REST_SERVICE_URI + "/user/" + QPM_ACCESS_TOKEN + tokenInfo.getAccess_token(),
                    request,
                    User.class);

        // Catch exception and return false if the username is already taken

        } catch (HttpClientErrorException e) {

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;

            } else if (e.getStatusCode() == HttpStatus.CONFLICT) {
                return false;
            }
        }

        // Check if uri length is valid and return true or false

        if (uri.toASCIIString().length() > (REST_SERVICE_URI + "/user/").length()) {
            return true;
        } else return false;
    }

    // Send a PUT request to update an existing user

    public Boolean updateUser(AuthTokenInfo tokenInfo, User theUser) {
        ResponseEntity<User> response = null;

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        HttpEntity<Object> request = new HttpEntity<Object>(theUser, getHeaders());

        try {
        response = restTemplate.exchange(
                REST_SERVICE_URI + "/user/" + theUser.getId() + QPM_ACCESS_TOKEN + tokenInfo.getAccess_token(),
                HttpMethod.PUT,
                request,
                User.class);

        // Catch exception and return false if the username is not found

        } catch (HttpClientErrorException e) {

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
        }

        if (response.getBody() != null) {
            return true;
        } else return null;
    }

    // Send a GET request to get list of all stations

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<Station> getStations(AuthTokenInfo tokenInfo) {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        HttpEntity<String> request = new HttpEntity<String>(getHeaders());

        ResponseEntity<List> response = restTemplate.exchange(
                REST_SERVICE_URI + "/stations/" + QPM_ACCESS_TOKEN + tokenInfo.getAccess_token(),
                HttpMethod.GET,
                request,
                List.class);

        // Convert the hash map from the response body into a list of stations

        List<LinkedHashMap<String, Object>> stationsMap = (List<LinkedHashMap<String, Object>>) response.getBody();
        List<Station> theStations = new ArrayList<Station>();

        if (stationsMap != null) {

            for(LinkedHashMap<String, Object> map : stationsMap){

                Station tempStation = new Station();
                tempStation.setId(Integer.parseInt(map.get("id").toString()));
                tempStation.setName((map.get("name")).toString());
                tempStation.setLatitude(Double.parseDouble(map.get("latitude").toString()));
                tempStation.setLongitude(Double.parseDouble(map.get("longitude").toString()));
                tempStation.setTrafficLevel(Integer.parseInt(map.get("trafficLevel").toString()));
                theStations.add(tempStation);
            }

        } else {
            System.out.println("No stations exist");
        }
        return theStations;
    }
}