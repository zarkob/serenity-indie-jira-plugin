package com.serenityindie.jiraplugin.processor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.serenityindie.jiraplugin.model.TestReport;
import com.serenityindie.jiraplugin.util.OffsetDateTimeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class SerenityReportProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerenityReportProcessor.class);
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
            .create();

    public List<TestReport> processSerenityReports(String serenityFolderPath) {
        LOGGER.info("Processing serenity reports in path {}..", serenityFolderPath);
        File serenityFolder = new File(serenityFolderPath);
        File[] jsonFiles = serenityFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json");
            }
        });

        List<TestReport> testSteps = new ArrayList<>();
        if (jsonFiles != null) {
            for (File jsonFile : jsonFiles) {
                try (FileReader reader = new FileReader(jsonFile)) {
                    TestReport testStep = GSON.fromJson(reader, TestReport.class);
                    testSteps.add(testStep);
                } catch (JsonParseException e) {
                    LOGGER.warn("Skipping file '{}': {}", jsonFile.getName(), e.getMessage());
                } catch (IOException e) {
                    LOGGER.error("Error reading file '{}': {}", jsonFile.getName(), e.getMessage());
                }
            }
        }
        return testSteps;
    }
}
