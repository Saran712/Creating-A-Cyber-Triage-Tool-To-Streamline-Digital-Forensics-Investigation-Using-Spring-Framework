package com.networkattack.demo.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.networkattack.demo.Model.NetworkAttack;
import com.networkattack.demo.Model.PreprocessedNetworkAttack;
import com.networkattack.demo.repository.NetworkAttackRepository;
import com.networkattack.demo.repository.PreprocessedNetworkAttackRepository;

import weka.core.Instances;

@Service
public class NetworkAttackService {

    private final NetworkAttackRepository repository;
    private final PreprocessedNetworkAttackRepository preprocessedRepository;

    // Constants for packet size categorization
    private static final int SMALL_PACKET_SIZE = 64;
    private static final int MEDIUM_PACKET_SIZE = 512;

    // List of known malicious IPs
    private static final List<String> MALICIOUS_IPS = List.of("192.168.1.100", "10.0.0.5");

    @Autowired
    public NetworkAttackService(NetworkAttackRepository repository, PreprocessedNetworkAttackRepository preprocessedRepository) {
        this.repository = repository;
        this.preprocessedRepository = preprocessedRepository;
    }

    // Process CSV file and save data to the database
    public void processCsvFile(MultipartFile file) throws Exception {
        List<NetworkAttack> attacks = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // Skip the header row
                    continue;
                }

                String[] data = line.split(",");
                NetworkAttack attack = new NetworkAttack();
                attack.setNo(data[0]);
                attack.setTime(data[1]);
                attack.setSource(data[2]);
                attack.setDestination(data[3]);
                attack.setProtocol(data[4]);
                attack.setLength(data[5]);
                attack.setInfo(data[6]);
                attacks.add(attack);
            }
        }
        repository.saveAll(attacks);
    }

    // Fetch all attacks from the database
    public List<NetworkAttack> getAllAttacks() {
        return repository.findAll();
    }

    // Delete an attack by ID
    public void deleteAttackById(Long id) {
        repository.deleteById(id);
    }

    // Preprocess the data and save it to the preprocessed table
    public void preprocessData() {
        List<NetworkAttack> attacks = repository.findAll();

        // Define the preprocessing pipeline
        List<Consumer<NetworkAttack>> pipeline = List.of(
            this::handleNullValues,          // Step 1: Handle null values
            this::removeDoubleQuotes,       // Step 2: Remove double quotes
            this::cleanInfoField,           // Step 3: Clean the "info" field
            this::standardizeProtocol,      // Step 4: Standardize protocol field
            this::detectMaliciousTraffic,   // Step 5: Detect malicious traffic
            this::categorizePacketSize,     // Step 6: Categorize packet size
            this::calculatePacketSentRate   // Step 7: Calculate packet sent rate
        );

        // Apply the pipeline to each attack
        attacks.forEach(attack -> pipeline.forEach(step -> step.accept(attack)));

        // Convert NetworkAttack to PreprocessedNetworkAttack
        List<PreprocessedNetworkAttack> preprocessedAttacks = attacks.stream()
            .map(this::convertToPreprocessed)
            .toList();

        // Save the preprocessed data to the new table
        preprocessedRepository.saveAll(preprocessedAttacks);
    }

    // Convert NetworkAttack to PreprocessedNetworkAttack
    private PreprocessedNetworkAttack convertToPreprocessed(NetworkAttack attack) {
        PreprocessedNetworkAttack preprocessed = new PreprocessedNetworkAttack();
        preprocessed.setNo(attack.getNo());
        preprocessed.setTime(attack.getTime());
        preprocessed.setSource(attack.getSource());
        preprocessed.setDestination(attack.getDestination());
        preprocessed.setProtocol(attack.getProtocol());
        preprocessed.setLength(attack.getLength());
        preprocessed.setInfo(attack.getInfo());

        // Map new fields
        preprocessed.setMalicious(attack.isMalicious());
        preprocessed.setPacketSizeCategory(attack.getPacketSizeCategory());
        preprocessed.setPacketSentRate(attack.getPacketSentRate());

        return preprocessed;
    }

    // Step 1: Handle null values
    private void handleNullValues(NetworkAttack attack) {
        if (attack.getNo() == null) attack.setNo("N/A");
        if (attack.getTime() == null) attack.setTime("N/A");
        if (attack.getSource() == null) attack.setSource("N/A");
        if (attack.getDestination() == null) attack.setDestination("N/A");
        if (attack.getProtocol() == null) attack.setProtocol("N/A");
        if (attack.getLength() == null) attack.setLength("N/A");
        if (attack.getInfo() == null) attack.setInfo("N/A");
    }

    // Step 2: Remove double quotes from each field
    private void removeDoubleQuotes(NetworkAttack attack) {
        if (attack.getNo() != null) attack.setNo(attack.getNo().replaceAll("\"", ""));
        if (attack.getTime() != null) attack.setTime(attack.getTime().replaceAll("\"", ""));
        if (attack.getSource() != null) attack.setSource(attack.getSource().replaceAll("\"", ""));
        if (attack.getDestination() != null) attack.setDestination(attack.getDestination().replaceAll("\"", ""));
        if (attack.getProtocol() != null) attack.setProtocol(attack.getProtocol().replaceAll("\"", ""));
        if (attack.getLength() != null) attack.setLength(attack.getLength().replaceAll("\"", ""));
        if (attack.getInfo() != null) attack.setInfo(attack.getInfo().replaceAll("\"", ""));
    }

    // Step 3: Clean the "info" field (remove special characters)
    private void cleanInfoField(NetworkAttack attack) {
        if (attack.getInfo() != null) {
            String cleanedInfo = attack.getInfo().replaceAll("[^a-zA-Z0-9\\s]", "");
            attack.setInfo(cleanedInfo);
        }
    }

    // Step 4: Standardize protocol field
    private void standardizeProtocol(NetworkAttack attack) {
        if (attack.getProtocol() != null) {
            attack.setProtocol(attack.getProtocol().toUpperCase());
        }
    }

    // Step 5: Detect malicious traffic
    private void detectMaliciousTraffic(NetworkAttack attack) {
        if (MALICIOUS_IPS.contains(attack.getSource())) {
            attack.setMalicious(true);
        } else {
            attack.setMalicious(false);
        }
    }
 
    // Step 6: Categorize packet size
    private void categorizePacketSize(NetworkAttack attack) {
        if (attack.getLength() != null && !attack.getLength().isEmpty()) {
            try {
                double length = Double.parseDouble(attack.getLength()); // Handle decimal values
                if (length <= SMALL_PACKET_SIZE) {
                    attack.setPacketSizeCategory("Small");
                } else if (length <= MEDIUM_PACKET_SIZE) {
                    attack.setPacketSizeCategory("Medium");
                } else {
                    attack.setPacketSizeCategory("Large");
                }
            } catch (NumberFormatException e) {
                attack.setPacketSizeCategory("Invalid");
            }
        } else {
            attack.setPacketSizeCategory("N/A");
        }
    }

    // Step 7: Calculate packet sent rate
    private void calculatePacketSentRate(NetworkAttack attack) {
        if (attack.getTime() != null && attack.getLength() != null) {
            try {
                String[] timeParts = attack.getTime().split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                int second = Integer.parseInt(timeParts[2]);
                int totalTimeInSeconds = hour * 3600 + minute * 60 + second;

                double length = Double.parseDouble(attack.getLength()); // Handle decimal values
                if (totalTimeInSeconds > 0) {
                    double packetSentRate = length / totalTimeInSeconds;
                    attack.setPacketSentRate(packetSentRate);
                } else {
                    attack.setPacketSentRate(0.0);
                }
            } catch (NumberFormatException e) {
                attack.setPacketSentRate(0.0); // Default value if parsing fails
            }
        } else {
            attack.setPacketSentRate(0.0); // Default value if fields are null
        }
    }

    public List<NetworkAttack> preprocessData(Instances data) {
        List<NetworkAttack> preprocessedData = new ArrayList<>();

        for (int i = 0; i < data.numInstances(); i++) {
            NetworkAttack attack = new NetworkAttack();

            // Assuming the dataset has the following columns:
            // 0: no, 1: time, 2: source, 3: destination, 4: protocol, 5: length, 6: info
            attack.setNo(data.instance(i).value(0) + ""); // Column 0: "no"
            attack.setTime(data.instance(i).value(1) + ""); // Column 1: "time"
            attack.setSource(data.instance(i).stringValue(2)); // Column 2: "source"
            attack.setDestination(data.instance(i).stringValue(3)); // Column 3: "destination"
            attack.setProtocol(data.instance(i).stringValue(4)); // Column 4: "protocol"
            attack.setLength(data.instance(i).value(5) + ""); // Column 5: "length"
            attack.setInfo(data.instance(i).stringValue(6)); // Column 6: "info"

            // Apply preprocessing steps
            handleNullValues(attack);
            removeDoubleQuotes(attack);
            cleanInfoField(attack);
            standardizeProtocol(attack);
            detectMaliciousTraffic(attack);
            categorizePacketSize(attack);
            calculatePacketSentRate(attack);

            preprocessedData.add(attack);
        }

        return preprocessedData;
    }
  

    // Save all attacks to the database
    public void saveAll(List<NetworkAttack> attacks) {
        repository.saveAll(attacks);
    }



}