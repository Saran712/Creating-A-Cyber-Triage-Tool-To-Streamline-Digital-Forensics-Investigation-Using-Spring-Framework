package com.networkattack.demo.ml;

import java.io.*;

public class DataCleaner {

    public static void cleanAndWriteData(String inputFilePath, String outputFilePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim(); // Remove leading/trailing spaces

                // Split the line into columns
                String[] columns = line.split(",");

                // Example: Ensure there are exactly 7 columns
                if (columns.length == 7) {
                    // Write the cleaned line to the output file
                    bw.write(line);
                    bw.newLine();
                } else {
                    System.err.println("Skipping invalid line: " + line);
                }
            }

            System.out.println("Data cleaned and written to: " + outputFilePath);
        }
    }
}