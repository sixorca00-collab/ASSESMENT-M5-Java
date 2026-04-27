package org.hotelNova.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class CsvExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvExporter.class);
    
    public static <T> CsvExportResult export(String filePath, String header, List<T> data, Function<T, String> mapper) {
        Objects.requireNonNull(filePath, "File path cannot be null");
        Objects.requireNonNull(header, "Header cannot be null");
        Objects.requireNonNull(data, "Data list cannot be null");
        Objects.requireNonNull(mapper, "Mapper function cannot be null");
        
        Path path = Paths.get(filePath);
        
        try {
            if (Files.exists(path)) {
                logger.warn("File already exists and will be overwritten: {}", filePath);
            }
            
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                logger.info("Created directory: {}", parentDir);
            }
            
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(escapeCsvField(header));
                writer.newLine();
                
                for (int i = 0; i < data.size(); i++) {
                    T item = data.get(i);
                    try {
                        String csvLine = mapper.apply(item);
                        if (csvLine != null) {
                            writer.write(escapeCsvLine(csvLine));
                            writer.newLine();
                        } else {
                            logger.warn("Mapper returned null for item at index {}", i);
                        }
                    } catch (Exception e) {
                        logger.error("Error processing item at index {}: {}", i, e.getMessage());
                        throw new CsvExportException("Error processing item at index " + i, e);
                    }
                }
            }
            
            logger.info("CSV exported successfully: {} ({} records)", filePath, data.size());
            return new CsvExportResult(true, filePath, data.size(), null);
            
        } catch (IOException e) {
            String errorMsg = "IO error exporting CSV to " + filePath;
            logger.error(errorMsg, e);
            return new CsvExportResult(false, filePath, 0, new CsvExportException(errorMsg, e));
        } catch (CsvExportException e) {
            logger.error("CSV export failed: {}", e.getMessage());
            return new CsvExportResult(false, filePath, 0, e);
        } catch (Exception e) {
            String errorMsg = "Unexpected error exporting CSV to " + filePath;
            logger.error(errorMsg, e);
            return new CsvExportResult(false, filePath, 0, new CsvExportException(errorMsg, e));
        }
    }
    
    private static String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
    
    private static String escapeCsvLine(String line) {
        if (line == null) {
            return "";
        }
        
        String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        StringBuilder escaped = new StringBuilder();
        
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                escaped.append(",");
            }
            escaped.append(escapeCsvField(fields[i]));
        }
        
        return escaped.toString();
    }
    
    public static class CsvExportResult {
        private final boolean success;
        private final String filePath;
        private final int recordCount;
        private final Exception error;
        
        public CsvExportResult(boolean success, String filePath, int recordCount, Exception error) {
            this.success = success;
            this.filePath = filePath;
            this.recordCount = recordCount;
            this.error = error;
        }
        
        public boolean isSuccess() { return success; }
        public String getFilePath() { return filePath; }
        public int getRecordCount() { return recordCount; }
        public Exception getError() { return error; }
        
        @Override
        public String toString() {
            if (success) {
                return String.format("CSV export successful: %s (%d records)", filePath, recordCount);
            } else {
                return String.format("CSV export failed: %s - %s", filePath, 
                    error != null ? error.getMessage() : "Unknown error");
            }
        }
    }
    
    public static class CsvExportException extends RuntimeException {
        public CsvExportException(String message) {
            super(message);
        }
        
        public CsvExportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}