package com.indium.assignment;

import com.indium.assignment.controller.MatchDataController;
import com.indium.assignment.service.MatchDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MatchDataController.class)

public class MatchDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchDataService matchDataService;

    @Test
    public void uploadJsonFileTest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "matches.json", "application/json", "{}".getBytes());

        mockMvc.perform(multipart("/api/cricket/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded and data saved successfully"));

        Mockito.verify(matchDataService, Mockito.times(1)).uploadJsonFile(any());
    }

    @Test
    public void uploadEmptyFileTest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "matches.json", "application/json", new byte[0]);

        mockMvc.perform(multipart("/api/cricket/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File is empty"));
    }

    @Test
    public void getMatchesByPlayerTest() throws Exception {
        String playerName = "Virat Kohli";
        int matchesPlayed = 100;

        when(matchDataService.getMatchesByPlayerName(playerName)).thenReturn(matchesPlayed);

        mockMvc.perform(get("/api/cricket/matches/player/" + playerName))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(matchesPlayed)));
    }

    @Test
    public void getCumulativeScoreTest() throws Exception {
        String playerName = "Virat Kohli";
        int cumulativeScore = 7000;

        when(matchDataService.getCumulativeScore(playerName)).thenReturn(cumulativeScore);

        mockMvc.perform(get("/api/cricket/player/" + playerName + "/cumulativeScore"))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(cumulativeScore)));
    }

    @Test
    public void getMatchScoresByDateTest() throws Exception {
        LocalDateTime matchDate = LocalDateTime.of(2008, 4, 24, 0, 0);
        String matchDateStr = "2008-04-24T00:00:00";
        when(matchDataService.getMatchScores(matchDate)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/cricket/matches/scores/" + matchDateStr)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getTopBatsmenTest() throws Exception {
        // Prepare test data
        Map<String, Object> batsman1 = new HashMap<>();
        batsman1.put("name", "Player1");
        batsman1.put("runs", 500);

        Map<String, Object> batsman2 = new HashMap<>();
        batsman2.put("name", "Player2");
        batsman2.put("runs", 400);

        List<Map<String, Object>> topBatsmen = Arrays.asList(batsman1, batsman2);

        // Mock the service call
        when(matchDataService.getTopBatsmen(any(Pageable.class))).thenReturn(topBatsmen);

        // Perform the request and verify the response
        mockMvc.perform(get("/api/cricket/topbatsmen")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Player1"))
                .andExpect(jsonPath("$[0].runs").value(500))
                .andExpect(jsonPath("$[1].name").value("Player2"))
                .andExpect(jsonPath("$[1].runs").value(400));
    }

}
