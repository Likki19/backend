package com.indium.assignment.controller;

import com.indium.assignment.service.MatchDataService;
import com.indium.assignment.entity.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cricket")
public class MatchDataController {

    @Autowired
    private MatchDataService matchDataService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadMatchData(@RequestParam("file") MultipartFile file) {
//        String message = matchUploadService.uploadMatches(file);
//        return new ResponseEntity<>(message, HttpStatus.OK);
//
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
        }

        try {
            // Pass MultipartFile directly to the service for handling
            matchDataService.uploadJsonFile(file);
            return ResponseEntity.status(HttpStatus.OK).body("File uploaded and data saved successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the file: " + e.getMessage());
        } catch (Exception e) {
            // Catching more general exceptions to debug issues
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + e.getMessage());
        }
    }
    @GetMapping("/matches/player/{playerName}")
    public Integer getMatchesByPlayer(@PathVariable String playerName) {
        return matchDataService.getMatchesByPlayerName(playerName);
    }

    @GetMapping("/player/{playerName}/cumulativeScore")
    public Integer getCumulativeScore(@PathVariable String playerName) {
        return matchDataService.getCumulativeScore(playerName);
    }
    /*
    @GetMapping("/matches/scores/dates") //?dates=2008-04-24T00:00:00
    public List<Object[]> getMatchScores(@PathVariable("dates") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dates) {
        return matchDataService.getMatchScores(dates);
    }
    */

    @GetMapping("/matches/scores/{dates}")
    public ResponseEntity<String> getMatchScores(@PathVariable("dates") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dates) {
        List<Object[]> scores = matchDataService.getMatchScores(dates);

        // Convert scores to a String format
        String response = scores.stream()
                .map(score -> "Match ID: " + score[0] + ", Score: " + score[1])
                .collect(Collectors.joining(", ", "Scores for matches on " + dates.toLocalDate() + ": ", ""));

        return ResponseEntity.ok(response);
    }


    @GetMapping("/topbatsmen")
    public List<Map<String, Object>> getTopBatsmen(Pageable pageable) {
        return matchDataService.getTopBatsmen(pageable);
    }

}
