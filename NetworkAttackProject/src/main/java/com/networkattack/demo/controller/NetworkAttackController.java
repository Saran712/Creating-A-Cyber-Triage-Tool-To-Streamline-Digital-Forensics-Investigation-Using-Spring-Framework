package com.networkattack.demo.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.networkattack.demo.Model.NetworkAttack;
import com.networkattack.demo.Model.PreprocessedNetworkAttack;
import com.networkattack.demo.ml.ModelService;
import com.networkattack.demo.repository.PreprocessedNetworkAttackRepository;
import com.networkattack.demo.service.NetworkAttackService;

@Controller
public class NetworkAttackController {

    private final NetworkAttackService service;
    private final PreprocessedNetworkAttackRepository preprocessedRepository;

    @Autowired
    public NetworkAttackController(NetworkAttackService service, PreprocessedNetworkAttackRepository preprocessedRepository) {
        this.service = service;
        this.preprocessedRepository = preprocessedRepository;
    }

    // Home Page
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("attacks", service.getAllAttacks());
        return "index";
    }

    // Upload Page
    @GetMapping("/upload")
    public String uploadPage() {
        return "upload"; // Return the upload page
    }

    // Handle File Upload
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload.");
            return "redirect:/upload";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<NetworkAttack> attacks = new ArrayList<>();
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // Skip the header row
                    continue;
                }

                String[] data = line.split(","); // Split CSV row by comma
                NetworkAttack attack = new NetworkAttack();
                attack.setNo(data[0]);
                attack.setTime(data[1]);
                attack.setSource(data[2]); // Remove double quotes from source
                attack.setDestination(data[3]); // Remove double quotes from destination
                attack.setProtocol(data[4]); // Remove double quotes from protocol
                attack.setLength(data[5]); // Remove double quotes from length
                attack.setInfo(data[6]); // Remove double quotes from info

                attacks.add(attack);
            }

            // Save the data to the database
            service.saveAll(attacks);

            // Add success message
            redirectAttributes.addFlashAttribute("message", "File uploaded and data stored successfully!");
        } catch (Exception e) {
            // Add error message
            redirectAttributes.addFlashAttribute("message", "Failed to upload file: " + e.getMessage());
        }

        return "redirect:/upload";
    }

    // View Dataset
    @GetMapping("/view")
    public String viewDataset(Model model) {
        model.addAttribute("attacks", service.getAllAttacks());
        return "view"; // Create a new Thymeleaf template named `view.html`
    }

    // Preprocess Data
    @GetMapping("/preprocess")
    public String preprocessData(Model model) {
        try {
            // Fetch the original data from the database
            List<NetworkAttack> attacks = service.getAllAttacks();

            // Preprocess the data
            List<PreprocessedNetworkAttack> preprocessedData = attacks.stream()
                .map(this::preprocessAttack) // Preprocess each attack
                .toList();

            // Add the preprocessed data to the model
            model.addAttribute("preprocessedData", preprocessedData);
            model.addAttribute("message", "Data preprocessed successfully!");
        } catch (Exception e) {
            model.addAttribute("message", "Failed to preprocess data: " + e.getMessage());
        }
        return "preprocessed-data"; // Return to the preprocessed-data view
    }

    // Preprocess a single NetworkAttack object
    private PreprocessedNetworkAttack preprocessAttack(NetworkAttack attack) {
        // Remove double quotes from each field
        if (attack.getNo() != null) attack.setNo(attack.getNo().replaceAll("\"", ""));
        if (attack.getTime() != null) attack.setTime(attack.getTime().replaceAll("\"", ""));
        if (attack.getSource() != null) attack.setSource(attack.getSource().replaceAll("\"", ""));
        if (attack.getDestination() != null) attack.setDestination(attack.getDestination().replaceAll("\"", ""));
        if (attack.getProtocol() != null) attack.setProtocol(attack.getProtocol().replaceAll("\"", ""));
        if (attack.getLength() != null) attack.setLength(attack.getLength().replaceAll("\"", ""));
        if (attack.getInfo() != null) attack.setInfo(attack.getInfo().replaceAll("\"", ""));

        // Clean the "info" field (remove special characters)
        String cleanedInfo = attack.getInfo().replaceAll("[^a-zA-Z0-9\\s]", "");
        attack.setInfo(cleanedInfo);

        // Create a PreprocessedNetworkAttack object
        PreprocessedNetworkAttack preprocessed = new PreprocessedNetworkAttack();
        preprocessed.setNo(attack.getNo());
        preprocessed.setTime(attack.getTime());
        preprocessed.setSource(attack.getSource());
        preprocessed.setDestination(attack.getDestination());
        preprocessed.setProtocol(attack.getProtocol());
        preprocessed.setLength(attack.getLength());
        preprocessed.setInfo(attack.getInfo());

        // Perform preprocessing steps
        preprocessed.setMalicious(detectMaliciousTraffic(attack.getSource()));
        preprocessed.setPacketSizeCategory(categorizePacketSize(attack.getLength()));
        preprocessed.setPacketSentRate(calculatePacketSentRate(attack.getTime(), attack.getLength()));

        return preprocessed;
    }

    // Detect malicious traffic
    private boolean detectMaliciousTraffic(String source) {
        List<String> maliciousIPs = List.of("192.168.1.100", "10.0.0.5");
        return maliciousIPs.contains(source);
    }

    // Categorize packet size
    private String categorizePacketSize(String length) {
        if (length != null && !length.isEmpty()) {
            try {
                int packetLength = Integer.parseInt(length);
                if (packetLength <= 64) {
                    return "Small";
                } else if (packetLength <= 512) {
                    return "Medium";
                } else {
                    return "Large";
                }
            } catch (NumberFormatException e) {
                return "Invalid";
            }
        }
        return "N/A";
    }

    // Calculate packet sent rate
    private double calculatePacketSentRate(String time, String length) {
        if (time != null && length != null) {
            try {
                String[] timeParts = time.split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                int second = Integer.parseInt(timeParts[2]);
                int totalTimeInSeconds = hour * 3600 + minute * 60 + second;
                if (totalTimeInSeconds > 0) {
                    return (double) Integer.parseInt(length) / totalTimeInSeconds;
                }
            } catch (Exception e) {
                // Handle parsing errors
            }
        }
        return 0.0;
    }

    @PostMapping("/delete/{id}")
    public String deleteAttack(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.deleteAttackById(id);
        redirectAttributes.addFlashAttribute("message", "Entry deleted successfully!");
        System.out.println("Redirecting with message: Entry deleted successfully!"); // Debug log
        return "redirect:/view";
    }
//    @Autowired
//    private ModelService modelService;
//
//    @GetMapping("/predict")
//    public String evaluateModel(Model model) {
//        try {
//            String datasetPath = "src/main/resources/data/network1_cleaned.csv";
//            Map<String, Object> evaluationResults = modelService.trainAndEvaluateModel(datasetPath);
//
//            // Debug: Print evaluation results
//            System.out.println("Summary: " + evaluationResults.get("summary"));
//            System.out.println("Accuracy: " + evaluationResults.get("accuracy"));
//            System.out.println("Confusion Matrix: " + Arrays.deepToString((int[][]) evaluationResults.get("confusionMatrix")));
//
//            // Add results to the Thymeleaf model
//            model.addAttribute("summary", evaluationResults.get("summary"));
//            model.addAttribute("accuracy", evaluationResults.get("accuracy"));
//            model.addAttribute("confusionMatrix", evaluationResults.get("confusionMatrix"));
//
//            return "evaluation";
//        } catch (Exception e) {
//            model.addAttribute("error", "Error evaluating model: " + e.getMessage());
//            return "error";
//        }
//    }

}