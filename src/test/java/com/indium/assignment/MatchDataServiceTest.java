
package com.indium.assignment;

import com.indium.assignment.entity.Match;
import com.indium.assignment.repository.MatchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indium.assignment.service.MatchDataService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class MatchDataServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MatchDataService matchDataService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUploadJsonFile_FileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.json", "application/json", new byte[0]);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            matchDataService.uploadJsonFile(emptyFile);
        });

        assertEquals("File is empty", exception.getMessage());
    }

    @Test
    public void testUploadJsonFile_InvalidJsonStructure() throws Exception {
        // Create a mock file with invalid JSON structure (missing required fields)
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "invalid.json",
                "application/json",
                "{\"info\": {}}".getBytes()
        );

        // Mock the behavior of objectMapper
        JsonNode mockJsonNode = new ObjectMapper().readTree(invalidFile.getInputStream());  // Use InputStream here
        when(objectMapper.readTree(any(InputStream.class))).thenReturn(mockJsonNode);

        // Call the method and check that it throws the expected exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            matchDataService.uploadJsonFile(invalidFile);
        });

        assertEquals("Invalid JSON structure: missing required fields", exception.getMessage());
    }

    @Test
    public void testUploadJsonFile_MatchAlreadyExists() throws Exception {
        // Create a mock file with valid JSON structure
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                "{\"info\": {\"event\": {\"match_number\": 1}}}".getBytes()
        );

        // Mock the behavior of objectMapper and matchRepository
        JsonNode mockJsonNode = new ObjectMapper().readTree(file.getInputStream());  // Use InputStream here
        when(objectMapper.readTree(any(InputStream.class))).thenReturn(mockJsonNode);
        when(matchRepository.findByMatchNumber(1)).thenReturn(Optional.of(new Match()));

        // Call the method under test
        matchDataService.uploadJsonFile(file);

        // Verify that the matchRepository was called and no further processing occurred
        verify(matchRepository, times(1)).findByMatchNumber(1);
        verify(matchRepository, never()).save(any());
    }


    @Test
    public void testGetMatchesByPlayerName() {
        String playerName = "John Doe";
        Integer matches = 100;
        when(matchRepository.countMatchesByPlayerName(playerName)).thenReturn(matches);

        Integer result = matchDataService.getMatchesByPlayerName(playerName);

        assertEquals(matches, result);
        verify(matchRepository, times(1)).countMatchesByPlayerName(playerName);
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    public void testGetCumulativeScore() {
        String playerName = "John Doe";
        Integer cumulativeScore = 100;
        when(matchRepository.getCumulativeScoreByPlayer(playerName)).thenReturn(cumulativeScore);

        Integer result = matchDataService.getCumulativeScore(playerName);

        assertEquals(cumulativeScore, result);
        verify(matchRepository, times(1)).getCumulativeScoreByPlayer(playerName);
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    public void testGetMatchScores() {
        LocalDateTime date = LocalDateTime.now();
        List<Object[]> scores = Collections.singletonList(new Object[]{});
        when(matchRepository.getMatchScoresByDate(date)).thenReturn(scores);

        List<Object[]> result = matchDataService.getMatchScores(date);

        assertEquals(scores, result);
        verify(matchRepository, times(1)).getMatchScoresByDate(date);
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }
    /*
    @Test
    public void testGetTopBatsmen() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Object[]> topBatsmen = new PageImpl<>(Collections.singletonList(new Object[]{}));
        when(matchRepository.findTopBatsmen(pageable)).thenReturn(topBatsmen);

        Page<Object[]> result = matchDataService.getTopBatsmen(pageable);

        assertEquals(topBatsmen, result);
        verify(matchRepository, times(1)).findTopBatsmen(pageable);
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

     */
}
