package com.networkattack.demo.ml.controller;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.networkattack.demo.ml.ModelService;

@Controller
public class ResultsController {

    private final ModelService modelService;

    // Constructor injection for ModelService
    public ResultsController(ModelService modelService) {
        this.modelService = modelService;
    }

    @GetMapping("/predict")
    public String showResults(Model model) {
        try {
            String datasetPath = "src/main/resources/data/network1_cleaned.csv";
            Map<String, Object> evaluationResults = modelService.trainAndEvaluateModel(datasetPath);

            // Add results to the model
            model.addAttribute("summary", evaluationResults.get("summary"));
            model.addAttribute("confusionMatrix", evaluationResults.get("confusionMatrix"));
            model.addAttribute("accuracy", evaluationResults.get("accuracy"));
            model.addAttribute("correctlyClassified", evaluationResults.get("correctlyClassified"));
            model.addAttribute("incorrectlyClassified", evaluationResults.get("incorrectlyClassified"));
            model.addAttribute("kappaStatistic", evaluationResults.get("kappaStatistic"));
            model.addAttribute("meanAbsoluteError", evaluationResults.get("meanAbsoluteError"));
            model.addAttribute("rootMeanSquaredError", evaluationResults.get("rootMeanSquaredError"));
            model.addAttribute("relativeAbsoluteError", evaluationResults.get("relativeAbsoluteError"));
            model.addAttribute("rootRelativeSquaredError", evaluationResults.get("rootRelativeSquaredError"));
            model.addAttribute("totalInstances", evaluationResults.get("totalInstances"));

            return "evaluation"; // This corresponds to evaluation.html
        } catch (Exception e) {
            model.addAttribute("error", "Error evaluating model: " + e.getMessage());
            return "error"; // This corresponds to error.html
        }
    }
}