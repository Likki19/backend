package com.indium.assignment;

import net.minidev.json.JSONObject;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MatchDataControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testUploadJsonFile() throws IOException {
        String url = "http://localhost:" + port + "/api/cricket/upload";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Load the test file from the classpath
        ClassPathResource fileResource = new ClassPathResource("335992.json");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = testRestTemplate.postForEntity(url, requestEntity, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("File uploaded and data saved successfully", response.getBody());
    }

    @Test
    public void testGetMatchesByPlayer() {
        String playerName = "V Kohli";
        String url = "http://localhost:" + port + "/api/cricket/matches/player/" + playerName;

        Integer response = testRestTemplate.getForObject(url, Integer.class);
        assertEquals(1, response);  // Expected number of matches played by V Kohli
    }

    @Test
    public void testGetCumulativeScore() {
        String playerName = "V Kohli";
        String url = "http://localhost:" + port + "/api/cricket/player/" + playerName + "/cumulativeScore";

        Integer response = testRestTemplate.getForObject(url, Integer.class);
        assertEquals(13, response);  // Expected cumulative score of V Kohli
    }

    @Test
    public void testGetMatchScoresByDate() {
        String date = "2008-04-24T00:00:00";
        String url = "http://localhost:" + port + "/api/cricket/matches/scores/" + date;

        ResponseEntity<String> response = testRestTemplate.getForEntity(url, String.class);
        String expectedResponse = "Scores for matches on 2008-04-24: Match ID: 9, Score: 431";
        // You can adjust the expectedResponse to match the actual response format.

        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void testGetTopBatsmen() throws Exception {
        String url = "http://localhost:" + port + "/api/cricket/topbatsmen?page=0&size=5";

        // Perform the actual request
        ResponseEntity<String> response = testRestTemplate.getForEntity(url, String.class);

        // Convert response body to JSON array
        JSONArray actualResponseArray = new JSONArray(response.getBody());

        // Define the expected response
        JSONArray expectedResponseArray = new JSONArray(
                "[" +
                        "{\"totalRuns\":0,\"playerName\":\"M Rawat\"}," +
                        "{\"totalRuns\":0,\"playerName\":\"R Dravid\"}," +
                        "{\"totalRuns\":0,\"playerName\":\"S Dhawan\"}," +
                        "{\"totalRuns\":1,\"playerName\":\"K Goel\"}," +
                        "{\"totalRuns\":1,\"playerName\":\"R Vinay Kumar\"}," +
                        "{\"totalRuns\":1,\"playerName\":\"RA Jadeja\"}," +
                        "{\"totalRuns\":1,\"playerName\":\"B Lee\"}," +
                        "{\"totalRuns\":1,\"playerName\":\"D Salunkhe\"}," +
                        "{\"totalRuns\":2,\"playerName\":\"S Chanderpaul\"}," +
                        "{\"totalRuns\":3,\"playerName\":\"SB Joshi\"}," +
                        "{\"totalRuns\":4,\"playerName\":\"MV Boucher\"}," +
                        "{\"totalRuns\":4,\"playerName\":\"Pankaj Singh\"}," +
                        "{\"totalRuns\":4,\"playerName\":\"KC Sangakkara\"}," +
                        "{\"totalRuns\":6,\"playerName\":\"VY Mahesh\"}," +
                        "{\"totalRuns\":6,\"playerName\":\"V Sehwag\"}," +
                        "{\"totalRuns\":7,\"playerName\":\"TM Srivastava\"}," +
                        "{\"totalRuns\":10,\"playerName\":\"Shahid Afridi\"}," +
                        "{\"totalRuns\":10,\"playerName\":\"IK Pathan\"}," +
                        "{\"totalRuns\":11,\"playerName\":\"JH Kallis\"}," +
                        "{\"totalRuns\":12,\"playerName\":\"Kamran Akmal\"}" +
                        "]"
        );
        }
    }


