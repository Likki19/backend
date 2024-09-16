package com.indium.assignment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indium.assignment.entity.*;
import com.indium.assignment.repository.*;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchDataService {

    private static final Logger logger = LoggerFactory.getLogger(MatchDataService.class);
    private static final String TOPIC = "match-logs-topic";

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OfficialRepository officialRepository;

    @Autowired
    private PowerplayRepository powerplayRepository;

    //private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;




    @CacheEvict(value = {"matchesByPlayer", "cumulativeScoreByPlayer", "matchScoresByDate", "topBatsmen"}, allEntries = true)
    @Transactional
    public void uploadJsonFile(MultipartFile file) throws IOException {
        //logger.info("Starting transaction for file: {}", file.getOriginalFilename());
        logger.info("File name: {}, size: {}", file.getOriginalFilename(), file.getSize());

        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            JsonNode rootNode = objectMapper.readTree(file.getInputStream());

            int matchNumber = rootNode.path("info").path("event").path("match_number").asInt();

            logger.debug("Parsed JSON: {}", rootNode.toPrettyString());

            // Check if the required fields are present
            if (!rootNode.has("info") || !rootNode.path("info").has("event") || !rootNode.path("info").path("event").has("match_number")) {
                throw new IllegalArgumentException("Invalid JSON structure: missing required fields");
            }





            Optional<Match> existingMatch = matchRepository.findByMatchNumber(matchNumber);
            if (existingMatch.isPresent()) {
                logger.warn("Match with match number {} already exists. Skipping data insertion.", matchNumber);
                return ;  // Exit the method to prevent duplication
            }

            Match match = parseAndSaveMatchData(rootNode);
            if (match == null) {
                throw new IllegalStateException("Failed to parse and save match data. Match is null.");
            }
            List<Team> teams = parseAndSaveTeamsData(rootNode, match);
            parseAndSavePlayersData(teams);
            parseAndSaveDeliveriesData(rootNode, match);
            parseAndSaveOfficialsData(rootNode, match);
            parseAndSavePowerplaysData(rootNode, match);



            logger.info("Successfully processed file: {}", file.getOriginalFilename());
        } catch (IOException e) {
            logger.error("Error processing file: {}, Message: {}", file.getOriginalFilename(), e.getMessage());
            throw new IOException("Failed to process JSON file", e);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument provided: {}, Message: {}", file.getOriginalFilename(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Internal error occurred: ", e);
            throw new RuntimeException("Internal error occurred", e);
        }
    }



    private Match parseAndSaveMatchData(JsonNode rootNode) {
        Match match = parseMatchData(rootNode);

        if (match == null) {
            logger.error("Failed to parse match data. Match is null.");
            throw new RuntimeException("Failed to parse match data. Invalid JSON structure.");
        }

        match = matchRepository.save(match);
        logger.info("Saved match with ID: {}", match.getMatchNumber());
        return match;
    }


    private Match parseMatchData(JsonNode rootNode) {
        logger.debug("Parsing match data");
        JsonNode infoNode = rootNode.path("info");

        // Check if required fields are missing
        if (infoNode.isMissingNode() || !infoNode.has("city") || !infoNode.has("match_number")) {
            logger.error("Invalid JSON structure: required fields 'city' or 'match_number' are missing");
            return null;
        }

        Match match = new Match();
        match.setCity(getTextValue(infoNode, "city"));
        match.setDates(parseDates(infoNode.path("dates")));
        parseEventDetails(infoNode.path("event"), match);
        match.setMatchType(getTextValue(infoNode, "match_type"));
        match.setGender(getTextValue(infoNode, "gender"));
        match.setSeason(getTextValue(infoNode, "season"));
        parseTossDetails(infoNode.path("toss"), match);
        parseOutcomeDetails(infoNode.path("outcome"), match);
        match.setOvers(infoNode.path("overs").asInt());
        match.setPlayerOfMatch(parsePlayerOfMatch(infoNode.path("player_of_match")));

        logger.debug("Parsed match data: match_number={}, city={}, date={}", match.getMatchNumber(), match.getCity(), match.getDates());
        return match;
    }


    private List<Team> parseAndSaveTeamsData(JsonNode rootNode, Match match) {
        List<Team> teams = parseTeamsData(rootNode, match);
        List<Team> savedTeams = teamRepository.saveAll(teams);
        logger.info("Saved {} teams", savedTeams.size());
        return savedTeams;
    }

    private List<Team> parseTeamsData(JsonNode rootNode, Match match) {
        logger.debug("Parsing teams and players data");
        List<Team> teams = new ArrayList<>();
        JsonNode teamsNode = rootNode.path("info").path("players");
        logger.debug("Teams node: {}", teamsNode);

        if (teamsNode.isObject()) {
            teamsNode.fields().forEachRemaining(entry -> {
                String teamName = entry.getKey();
                JsonNode playersNode = entry.getValue();

                Team team = new Team();
                team.setTeamName(teamName);
                team.setMatch(match);
                logger.debug("Parsing team: {}", teamName);

                List<Player> players = new ArrayList<>();
                if (playersNode.isArray()) {
                    for (JsonNode playerNode : playersNode) {
                        Player player = new Player();
                        player.setPlayerName(playerNode.asText());
                        player.setTeam(team);
                        player.setMatch(match);
                        players.add(player);
                        logger.debug("Player added: {}", player.getPlayerName());
                    }
                } else {
                    logger.warn("Players node for team {} is not an array", teamName);
                }

                team.setPlayers(players);
                teams.add(team);
            });
        } else {
            logger.warn("Teams node is not an object or is missing");
        }

        logger.debug("Teams parsed: count = {}", teams.size());
        return teams;
    }

    private void parseAndSavePlayersData(List<Team> teams) {
        List<Player> allPlayers = teams.stream()
                .flatMap(team -> team.getPlayers().stream())
                .collect(Collectors.toList());
        List<Player> savedPlayers = playerRepository.saveAll(allPlayers);
        logger.info("Saved {} players", savedPlayers.size());
    }

    private void parseAndSaveDeliveriesData(JsonNode rootNode, Match match) {
        List<Delivery> deliveries = parseDeliveriesData(rootNode, match);
        List<Delivery> savedDeliveries = deliveryRepository.saveAll(deliveries);
        logger.info("Saved {} deliveries", savedDeliveries.size());
    }

    private List<Delivery> parseDeliveriesData(JsonNode rootNode, Match match) {
        logger.debug("Parsing deliveries data");
        List<Delivery> deliveries = new ArrayList<>();
        JsonNode inningsNode = rootNode.path("innings");
        logger.debug("Innings node: {}", inningsNode);

        if (inningsNode.isArray()) {
            for (JsonNode inning : inningsNode) {
                JsonNode overs = inning.path("overs");
                for (JsonNode over : overs) {
                    int overNumber = over.path("over").asInt();
                    JsonNode deliveriesNode = over.path("deliveries");
                    for (JsonNode deliveryNode : deliveriesNode) {
                        Delivery delivery = new Delivery();
                        delivery.setOverNumber(overNumber);
                        delivery.setBallNumber(deliveryNode.path("ball").asInt());
                        delivery.setBowler(deliveryNode.path("bowler").asText());
                        delivery.setNonStriker(deliveryNode.path("non_striker").asText());

                        // Extract and set batter's name
                        String batterName = deliveryNode.path("batter").asText();
                        if (batterName == null || batterName.isEmpty()) {
                            logger.warn("Batter name is null or empty, skipping delivery.");
                            continue;  // Skip deliveries with missing or null batter names
                        }

                        try {
                            Player batter = getOrCreatePlayer(batterName);
                            delivery.setPlayer(batter);  // Set the batter for this delivery
                            delivery.setBatter(batter.getPlayerName());
                        } catch (Exception e) {
                            logger.error("Failed to fetch or create player for batter: " + batterName, e);
                            continue;  // Skip this delivery if batter creation fails
                        }

                        // Extract run details
                        JsonNode runsNode = deliveryNode.path("runs");
                        delivery.setRunsBatter(runsNode.path("batter").asInt());
                        delivery.setRunsExtras(runsNode.path("extras").asInt());
                        delivery.setRunsTotal(runsNode.path("total").asInt());

                        // Set the match for the delivery
                        delivery.setMatch(match);

                        deliveries.add(delivery);
                    }
                }
            }
        } else {
            logger.warn("Innings node is not an array or is missing");
        }

        logger.debug("Deliveries parsed: count = {}", deliveries.size());
        return deliveries;
    }



    private Player getOrCreatePlayer(String playerName) {
        List<Player> players = playerRepository.findAllByPlayerName(playerName);
        if (players.isEmpty()) {
            Player newPlayer = new Player();
            newPlayer.setPlayerName(playerName);
            return playerRepository.save(newPlayer);
        } else if (players.size() == 1) {
            return players.get(0);
        } else {
            logger.warn("Multiple players found with name: {}. Using the first one.", playerName);
            return players.get(0);
        }
    }




    private void parseAndSaveOfficialsData(JsonNode rootNode, Match match) {
        List<Official> officials = parseOfficialsData(rootNode, match);
        List<Official> savedOfficials = officialRepository.saveAll(officials);
        logger.info("Saved {} officials", savedOfficials.size());
    }

    private List<Official> parseOfficialsData(JsonNode rootNode, Match match) {
        logger.debug("Parsing officials data");
        List<Official> officials = new ArrayList<>();
        JsonNode officialsNode = rootNode.path("info").path("officials");
        logger.debug("Officials node: {}", officialsNode);

        if (officialsNode.isObject()) {
            for (String officialType : new String[]{"umpires", "referee"}) {
                JsonNode officialTypeNode = officialsNode.path(officialType);
                if (officialTypeNode.isArray()) {
                    for (JsonNode officialNode : officialTypeNode) {
                        Official official = new Official();
                        official.setOfficialType(officialType);
                        official.setOfficialName(officialNode.asText());
                        official.setMatch(match);
                        officials.add(official);
                        logger.debug("Official parsed: {} - {}", officialType, official.getOfficialName());
                    }
                }
            }
        } else {
            logger.warn("Officials node is not an object or is missing");
        }

        logger.debug("Officials parsed: count = {}", officials.size());
        return officials;
    }

    private void parseAndSavePowerplaysData(JsonNode rootNode, Match match) {
        List<Powerplay> powerplays = parsePowerplaysData(rootNode, match);
        List<Powerplay> savedPowerplays = powerplayRepository.saveAll(powerplays);
        logger.info("Saved {} powerplays", savedPowerplays.size());
    }

    private List<Powerplay> parsePowerplaysData(JsonNode rootNode, Match match) {
        logger.debug("Parsing powerplays data");
        List<Powerplay> powerplays = new ArrayList<>();
        JsonNode inningsNode = rootNode.path("innings");
        logger.debug("Innings node: {}", inningsNode);

        if (inningsNode.isArray()) {
            for (JsonNode inning : inningsNode) {
                JsonNode powerplaysNode = inning.path("powerplays");
                if (powerplaysNode.isArray()) {
                    for (JsonNode powerplayNode : powerplaysNode) {
                        Powerplay powerplay = new Powerplay();
                        powerplay.setFromOver(powerplayNode.path("from").asDouble());
                        powerplay.setToOver(powerplayNode.path("to").asDouble());
                        powerplay.setType(powerplayNode.path("type").asText());
                        powerplay.setMatch(match);
                        powerplays.add(powerplay);
                        logger.debug("Powerplay parsed: {} - {}", powerplay.getFromOver(), powerplay.getToOver());
                    }
                }
            }
        } else {
            logger.warn("Innings node is not an array or is missing");
        }

        logger.debug("Powerplays parsed: count = {}", powerplays.size());
        return powerplays;
    }

    private String getTextValue(JsonNode node, String fieldName) {
        return node.path(fieldName).asText("");
    }

    private LocalDateTime parseDates(JsonNode datesNode) {
        if (datesNode.isArray() && datesNode.size() > 0) {
            String dateStr = datesNode.get(0).asText();
            return LocalDateTime.parse(dateStr + "T00:00:00");
        }
        return null;
    }

    private void parseEventDetails(JsonNode eventNode, Match match) {
        match.setMatchNumber(eventNode.path("match_number").asInt());
        match.setEventName(getTextValue(eventNode, "name"));
    }

    private void parseTossDetails(JsonNode tossNode, Match match) {
        match.setTossWinner(getTextValue(tossNode, "winner"));
        match.setTossDecision(getTextValue(tossNode, "decision"));
    }

    private void parseOutcomeDetails(JsonNode outcomeNode, Match match) {
        match.setWinner(getTextValue(outcomeNode, "winner"));
        match.setOutcomeByWickets(outcomeNode.path("by").path("wickets").asInt());
    }

    private String parsePlayerOfMatch(JsonNode playerOfMatchNode) {
        if (playerOfMatchNode.isArray() && playerOfMatchNode.size() > 0) {
            return playerOfMatchNode.get(0).asText();
        }
        return "";
    }

   // @Transactional
    @Cacheable(value = "matchesByPlayer", key = "#playerName")
    public Integer getMatchesByPlayerName(String playerName) {
        logger.info("Fetching matches for player: {}", playerName);
        sendLogToKafka("getMatchesByPlayerName", "playerName", playerName);
        //return matchRepository.findMatchesByPlayerName(playerName);
        Integer matches = matchRepository.countMatchesByPlayerName(playerName);
        return matches;
    }

   // @Transactional
    @Cacheable(value = "cumulativeScoreByPlayer", key = "#playerName", unless = "#result == null")
    public int getCumulativeScore(String playerName) {
        logger.info("Calculating cumulative score for player: {}", playerName);
        sendLogToKafka("getCumulativeScore", "playerName", playerName);
        return matchRepository.getCumulativeScoreByPlayer(playerName);
    }

    //@Transactional

    @Cacheable(value = "matchScoresByDate", key = "#dates" ,unless="#result == null")
    public List<Object[]> getMatchScores(LocalDateTime dates) {
        sendLogToKafka("getMatchScores", "dates", dates.toString());
        return matchRepository.getMatchScoresByDate(dates);
    }

    public List<Map<String, Object>> getTopBatsmen(Pageable pageable) {
        sendLogToKafka("getTopBatsmen", "pageable", pageable.toString());
        Page<Object[]> topBatsmenPage = matchRepository.findTopBatsmen(pageable);

        // Process the result to return only the player name and scores
        return topBatsmenPage.getContent().stream()
                .map(row -> Map.of("playerName", row[0], "totalRuns", row[1])) // row[0] is player name, row[1] is total runs
                .collect(Collectors.toList());
    }
    @Transactional
    private void sendLogToKafka(String methodName, String paramKey, String paramValue) {
        try {
            Map<String, Object> logMessage = new HashMap<>();
            logMessage.put("method", methodName);
            logMessage.put("timestamp", LocalDateTime.now().toString());
            logMessage.put("params", Map.of(paramKey, paramValue));

            String jsonLog = objectMapper.writeValueAsString(logMessage);

            // Send log to Kafka
            kafkaTemplate.send(new ProducerRecord<>(TOPIC, jsonLog));
            logger.info("Log sent to Kafka: {}", jsonLog);

        } catch (Exception e) {
            logger.error("Failed to send log to Kafka", e);
        }
    }
    }



