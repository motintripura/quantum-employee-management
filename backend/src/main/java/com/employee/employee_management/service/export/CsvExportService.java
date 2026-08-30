package com.employee.employee_management.service.export;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CsvExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    public byte[] generateCsv(List<String> headers, List<Object[]> rows) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(UTF8_BOM);

            OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
            CSVWriter csvWriter = new CSVWriter(writer);

            writeHeaderRow(csvWriter, headers);
            writeDataRows(csvWriter, rows);

            csvWriter.flush();
            csvWriter.close();

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV", e);
        }
    }

    private void writeHeaderRow(CSVWriter csvWriter, List<String> headers) {
        if (headers == null || headers.isEmpty()) return;

        String[] headerArray = headers.toArray(new String[0]);
        csvWriter.writeNext(headerArray);
    }

    private void writeDataRows(CSVWriter csvWriter, List<Object[]> rows) {
        if (rows == null) return;

        for (Object[] row : rows) {
            String[] csvRow = new String[row.length];
            for (int i = 0; i < row.length; i++) {
                csvRow[i] = formatCellValue(row[i]);
            }
            csvWriter.writeNext(csvRow);
        }
    }

    private String formatCellValue(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DATE_FORMATTER);
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DATETIME_FORMATTER);
        } else if (value instanceof LocalTime) {
            return ((LocalTime) value).format(TIME_FORMATTER);
        } else if (value instanceof Enum) {
            return ((Enum<?>) value).name();
        } else {
            return value.toString();
        }
    }
}
