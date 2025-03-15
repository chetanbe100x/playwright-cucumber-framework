package com.framework.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Utility class to read test data from Excel files
 */
public class ExcelReader {
    private static final LoggerUtil LOGGER = new LoggerUtil(ExcelReader.class);
    private String filePath;
    private Workbook workbook;
    private Map<String, Map<String, Map<String, String>>> testData;

    /**
     * Initializes the ExcelReader with the specified file path
     *
     * @param filePath Path to the Excel file
     */
    public ExcelReader(String filePath) {
        this.filePath = filePath;
        this.testData = new HashMap<>();
        loadWorkbook();
    }

    /**
     * Loads the Excel workbook from the file path
     */
    private void loadWorkbook() {
        try {
            LOGGER.info("Loading Excel workbook: " + filePath);
            FileInputStream fis = new FileInputStream(filePath);
            workbook = new XSSFWorkbook(fis);
            fis.close();
        } catch (IOException e) {
            LOGGER.error("Failed to load Excel workbook: " + filePath, e);
            throw new RuntimeException("Failed to load Excel workbook: " + filePath, e);
        }
    }

    /**
     * Loads test data from a specific sheet in the Excel file
     *
     * @param sheetName Name of the sheet to load
     * @param keyColumn Column to use as the key for test data rows
     */
    public void loadTestData(String sheetName, String keyColumn) {
        LOGGER.info("Loading test data from sheet: " + sheetName);
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            LOGGER.error("Sheet not found: " + sheetName);
            throw new RuntimeException("Sheet not found: " + sheetName);
        }

        // Get header row
        Row headerRow = sheet.getRow(0);
        Map<Integer, String> headers = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                headers.put(i, cell.getStringCellValue().trim());
            }
        }

        // Find key column index
        int keyColumnIndex = -1;
        for (Map.Entry<Integer, String> entry : headers.entrySet()) {
            if (entry.getValue().equals(keyColumn)) {
                keyColumnIndex = entry.getKey();
                break;
            }
        }

        if (keyColumnIndex == -1) {
            LOGGER.error("Key column not found: " + keyColumn);
            throw new RuntimeException("Key column not found: " + keyColumn);
        }

        // Process data rows
        Map<String, Map<String, String>> sheetData = new HashMap<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell keyCell = row.getCell(keyColumnIndex);
            if (keyCell == null) continue;

            String key = getCellValueAsString(keyCell);
            Map<String, String> rowData = new HashMap<>();

            for (int j = 0; j < headers.size(); j++) {
                Cell cell = row.getCell(j);
                if (cell != null && headers.get(j) != null) {
                    rowData.put(headers.get(j), getCellValueAsString(cell));
                }
            }

            sheetData.put(key, rowData);
        }

        testData.put(sheetName, sheetData);
        LOGGER.info("Loaded " + sheetData.size() + " rows of test data from sheet: " + sheetName);
    }

    /**
     * Gets a specific test data value
     *
     * @param sheetName Name of the sheet
     * @param rowKey Row key
     * @param columnName Column name
     * @return Test data value
     */
    public String getTestData(String sheetName, String rowKey, String columnName) {
        Map<String, Map<String, String>> sheetData = testData.get(sheetName);
        if (sheetData == null) {
            LOGGER.error("Sheet data not loaded: " + sheetName);
            throw new RuntimeException("Sheet data not loaded: " + sheetName);
        }

        Map<String, String> rowData = sheetData.get(rowKey);
        if (rowData == null) {
            LOGGER.error("Row key not found: " + rowKey);
            throw new RuntimeException("Row key not found: " + rowKey);
        }

        String value = rowData.get(columnName);
        if (value == null) {
            LOGGER.error("Column not found: " + columnName);
            throw new RuntimeException("Column not found: " + columnName);
        }

        return value;
    }

    /**
     * Gets all test data for a specific row
     *
     * @param sheetName Name of the sheet
     * @param rowKey Row key
     * @return Map of column names to values
     */
    public Map<String, String> getRowData(String sheetName, String rowKey) {
        Map<String, Map<String, String>> sheetData = testData.get(sheetName);
        if (sheetData == null) {
            LOGGER.error("Sheet data not loaded: " + sheetName);
            throw new RuntimeException("Sheet data not loaded: " + sheetName);
        }

        Map<String, String> rowData = sheetData.get(rowKey);
        if (rowData == null) {
            LOGGER.error("Row key not found: " + rowKey);
            throw new RuntimeException("Row key not found: " + rowKey);
        }

        return new HashMap<>(rowData);
    }

    /**
     * Converts a cell value to a string
     *
     * @param cell Cell to get value from
     * @return Cell value as string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    // Check if it's an integer or decimal
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value)) {
                        return String.valueOf((int) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return getCellValueAsString(workbook.getCreationHelper().createFormulaEvaluator().evaluateInCell(cell));
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            default:
                return "";
        }
    }

    /**
     * Closes the workbook and releases resources
     */
    public void close() {
        try {
            if (workbook != null) {
                workbook.close();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to close workbook", e);
        }
    }
}