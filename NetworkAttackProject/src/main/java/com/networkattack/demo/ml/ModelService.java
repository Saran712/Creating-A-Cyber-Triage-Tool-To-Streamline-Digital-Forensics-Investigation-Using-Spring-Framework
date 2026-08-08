package com.networkattack.demo.ml;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.networkattack.demo.Model.NetworkAttack;
import com.networkattack.demo.service.NetworkAttackService;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

@Service
public class ModelService {

    @Autowired
    private NetworkAttackService networkAttackService;

    public Map<String, Object> trainAndEvaluateModel(String datasetPath) throws Exception {
        Map<String, Object> result = new HashMap<>();

        try {
            // Clean the data and create the cleaned file
            String inputFilePath = "src/main/resources/data/network1.csv";
            String outputFilePath = "src/main/resources/data/network1_cleaned.csv";
            DataCleaner.cleanAndWriteData(inputFilePath, outputFilePath);

            // Load the cleaned dataset
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File(outputFilePath));
            Instances data = loader.getDataSet();

            // Ensure the dataset has the correct structure
            if (data.numAttributes() < 7) {
                throw new Exception("Dataset must have at least 7 attributes (columns).");
            }

            // Set the class attribute (assuming the last column is the class label)
            data.setClassIndex(data.numAttributes() - 1);

            // Preprocess the data using NetworkAttackService
            List<NetworkAttack> preprocessedData = networkAttackService.preprocessData(data);

            // Train Random Forest classifier
            RandomForest rf = new RandomForest();
            rf.buildClassifier(data);

            // Evaluate the model
            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(rf, data, 10, new java.util.Random(1));

            // Store evaluation results
            result.put("summary", eval.toSummaryString("\nResults\n======\n", false));
            result.put("confusionMatrix", eval.confusionMatrix());
            result.put("accuracy", eval.pctCorrect());
            result.put("correctlyClassified", (int) eval.correct());
            result.put("incorrectlyClassified", (int) eval.incorrect());
            result.put("kappaStatistic", eval.kappa());
            result.put("meanAbsoluteError", eval.meanAbsoluteError());
            result.put("rootMeanSquaredError", eval.rootMeanSquaredError());
            result.put("relativeAbsoluteError", eval.relativeAbsoluteError());
            result.put("rootRelativeSquaredError", eval.rootRelativeSquaredError());
            result.put("totalInstances", eval.numInstances());

        } catch (Exception e) {
            throw new Exception("Error evaluating model: " + e.getMessage(), e);
        }

        return result;
    }
}