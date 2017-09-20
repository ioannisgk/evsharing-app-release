package com.ioannisgk.evsharingapp.utils;

import android.util.Base64;

import com.ioannisgk.evsharingapp.entities.User;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class SpringRestClient {

    public static final String REST_SERVICE_URI = "http://138.68.159.75:8080/evsharing-platform/api";
    public static final String AUTH_SERVER_URI = "http://138.68.159.75:8080/evsharing-platform/oauth/token";
    public static final String QPM_PASSWORD_GRANT = "?grant_type=password&username=evsharingUser&password=evsharingPass";
    public static final String QPM_ACCESS_TOKEN = "?access_token=";

    // Class constructor
    public SpringRestClient() {
        System.out.println("OUT0: ");
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

        System.out.println("OUT3:1 ");
        ResponseEntity<Object> response = restTemplate.exchange(
                AUTH_SERVER_URI + QPM_PASSWORD_GRANT,
                HttpMethod.POST,
                request,
                Object.class);

        System.out.println("OUT3:2 ");
        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)response.getBody();
        AuthTokenInfo tokenInfo = null;

        if(map != null){

            tokenInfo = new AuthTokenInfo();
            tokenInfo.setAccess_token((String)map.get("access_token"));
            tokenInfo.setToken_type((String)map.get("token_type"));
            tokenInfo.setRefresh_token((String)map.get("refresh_token"));
            tokenInfo.setExpires_in((int)map.get("expires_in"));
            tokenInfo.setScope((String)map.get("scope"));
            System.out.println(tokenInfo);

        } else {
            System.out.println("No user exist----------");
        }
        return tokenInfo;
    }

    // Send a POST request to login a user

    public User loginUser(AuthTokenInfo tokenInfo) {

        Assert.notNull(tokenInfo, "Authenticate first please......");
        System.out.println("\n2. Testing login API----------");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        // Create the request body as a hash map to store the username and password
        LinkedHashMap<String, String> body = new LinkedHashMap<>();

        body.put("username", "user111");
        body.put("password", "pass111");

        HttpEntity<Object> request = new HttpEntity<Object>(body, getHeaders());
        ResponseEntity<User> response = restTemplate.exchange(
                REST_SERVICE_URI + "/login/" + QPM_ACCESS_TOKEN + tokenInfo.getAccess_token(),
                HttpMethod.POST,
                request,
                User.class);

        System.out.println(response.getBody());

        return response.getBody();

    }

    // Send a GET request to get list of all users

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void listAllUsers(AuthTokenInfo tokenInfo){

        Assert.notNull(tokenInfo, "Authenticate first please......");
        System.out.println("\n1. Testing listAllUsers API-----------");

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<String> request = new HttpEntity<String>(getHeaders());
        ResponseEntity<List> response = restTemplate.exchange(
                REST_SERVICE_URI + "/users/" + QPM_ACCESS_TOKEN + tokenInfo.getAccess_token(),
                HttpMethod.GET,
                request,
                List.class);

        List<LinkedHashMap<String, Object>> usersMap = (List<LinkedHashMap<String, Object>>) response.getBody();

        if(usersMap != null){

            for(LinkedHashMap<String, Object> map : usersMap){
                System.out.println("User : id = " + map.get("id") +
                        ", Name = " + map.get("name") +
                        ", Username = " + map.get("username") +
                        ", Password = " + map.get("password") +
                        ", Gender = " + map.get("gender") +
                        ", Date of Birth = " + map.get("dob"));
            }

        } else {
            System.out.println("No user exist----------");
        }
    }

    // Send a GET request to get a specific user

    private static void getUser(AuthTokenInfo tokenInfo){

        Assert.notNull(tokenInfo, "Authenticate first please......");
        System.out.println("\n2. Testing getUser API----------");

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<String> request = new HttpEntity<String>(getHeaders());
        ResponseEntity<User> response = restTemplate.exchange(
                REST_SERVICE_URI + "/user/4" + QPM_ACCESS_TOKEN + tokenInfo.getAccess_token(),
                HttpMethod.GET,
                request,
                User.class);

        User theUser = response.getBody();
        System.out.println(theUser);
    }

    // Send a POST request to create a new user

    private static void createUser(AuthTokenInfo tokenInfo) {

        Assert.notNull(tokenInfo, "Authenticate first please......");
        System.out.println("\n3. Testing create User API----------");

        RestTemplate restTemplate = new RestTemplate();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDob = null;
        try {
            currentDob = format.parse("02/02/1880");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        User theUser = new User("usernameTest3677.", "passwordTest36.", "Name Surname", "MALE", currentDob);

        HttpEntity<Object> request = new HttpEntity<Object>(theUser, getHeaders());
        URI uri = restTemplate.postForLocation(
                REST_SERVICE_URI + "/user/" + QPM_ACCESS_TOKEN + tokenInfo.getAccess_token(),
                request,
                User.class);

        System.out.println("Location : " + uri.toASCIIString());
    }

    // Send a PUT request to update an existing user

    private static void updateUser(AuthTokenInfo tokenInfo) {

        Assert.notNull(tokenInfo, "Authenticate first please......");
        System.out.println("\n4. Testing update User API----------");

        RestTemplate restTemplate = new RestTemplate();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDob = null;
        try {
            currentDob = format.parse("02/02/1990");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        User theUser  = new User("01usernameTest8s8", "01passwordTest8s8", "01WSSSWffff", "MALE", currentDob);

        HttpEntity<Object> request = new HttpEntity<Object>(theUser, getHeaders());
        ResponseEntity<User> response = restTemplate.exchange(
                REST_SERVICE_URI + "/user/4" + QPM_ACCESS_TOKEN + tokenInfo.getAccess_token(),
                HttpMethod.PUT,
                request,
                User.class);

        System.out.println(response.getBody());
    }

    // Send a DELETE request to delete a specific user

    private static void deleteUser(AuthTokenInfo tokenInfo) {

        Assert.notNull(tokenInfo, "Authenticate first please......");
        System.out.println("\n5. Testing delete User API----------");

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<String> request = new HttpEntity<String>(getHeaders());
        restTemplate.exchange(
                REST_SERVICE_URI + "/user/4" + QPM_ACCESS_TOKEN + tokenInfo.getAccess_token(),
                HttpMethod.DELETE,
                request, User.class);
    }

    public User login() {
        AuthTokenInfo tokenInfo = sendTokenRequest();
        return loginUser(tokenInfo);
    }

    //public static void main(String args[]){

        //AuthTokenInfo tokenInfo = sendTokenRequest();
        //listAllUsers(tokenInfo);

        //loginUser(tokenInfo);

        //getUser(tokenInfo);

        //createUser(tokenInfo);
        //listAllUsers(tokenInfo);

        //updateUser(tokenInfo);
        //listAllUsers(tokenInfo);

        //deleteUser(tokenInfo);
        //listAllUsers(tokenInfo);
   // }
}