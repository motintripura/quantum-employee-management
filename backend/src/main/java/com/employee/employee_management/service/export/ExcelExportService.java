package com.employee.employee_management.service.export;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public byte[] generateExcel(String title, String sheetName, List<String> headers,
                                List<Object[]> rows, String filterSummary) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(sheetName);

            int currentRow = 0;
            currentRow = addTitleRow(workbook, sheet, title, currentRow, headers.size());
            currentRow = addDateRow(workbook, sheet, currentRow, headers.size());
            currentRow = addFilterRow(workbook, sheet, filterSummary, currentRow, headers.size());
            currentRow++; // blank row
            currentRow = addHeaderRow(workbook, sheet, headers, currentRow);
            currentRow = addDataRows(workbook, sheet, rows, currentRow);
            autoSizeColumns(sheet, headers.size());

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    private int addTitleRow(Workbook workbook, Sheet sheet, String title, int rowIndex, int columnCount) {
        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(30);
        CellStyle titleStyle = createTitleStyle(workbook);

        CellRangeAddress mergedRange = new CellRangeAddress(rowIndex, rowIndex, 0, columnCount - 1);
        sheet.addMergedRegion(mergedRange);

        org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(titleStyle);
        return rowIndex + 1;
    }

    private int addDateRow(Workbook workbook, Sheet sheet, int rowIndex, int columnCount) {
        Row row = sheet.createRow(rowIndex);
        CellStyle dateStyle = createDateStyle(workbook);

        CellRangeAddress mergedRange = new CellRangeAddress(rowIndex, rowIndex, 0, columnCount - 1);
        sheet.addMergedRegion(mergedRange);

        org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
        cell.setCellValue("Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER));
        cell.setCellStyle(dateStyle);
        return rowIndex + 1;
    }

    private int addFilterRow(Workbook workbook, Sheet sheet, String filterSummary,
                             int rowIndex, int columnCount) {
        if (filterSummary == null || filterSummary.isBlank()) {
            return rowIndex;
        }

        Row row = sheet.createRow(rowIndex);
        CellStyle filterStyle = createFilterStyle(workbook);

        CellRangeAddress mergedRange = new CellRangeAddress(rowIndex, rowIndex, 0, columnCount - 1);
        sheet.addMergedRegion(mergedRange);

        org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
        cell.setCellValue("Filters: " + filterSummary);
        cell.setCellStyle(filterStyle);
        return rowIndex + 1;
    }

    private int addHeaderRow(Workbook workbook, Sheet sheet, List<String> headers, int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(22);
        CellStyle headerStyle = createHeaderStyle(workbook);

        for (int i = 0; i < headers.size(); i++) {
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }
        return rowIndex + 1;
    }

    private int addDataRows(Workbook workbook, Sheet sheet, List<Object[]> rows, int startIndex) {
        if (rows == null) return startIndex;

        DataFormat dataFormat = workbook.createDataFormat();
        CellStyle currencyStyle = createCurrencyStyle(workbook, dataFormat);
        CellStyle dateStyle = createCellDateStyle(workbook, dataFormat);
        CellStyle dateTimeStyle = createCellDateTimeStyle(workbook, dataFormat);
        CellStyle timeStyle = createTimeStyle(workbook, dataFormat);
        CellStyle integerStyle = createIntegerStyle(workbook, dataFormat);
        CellStyle defaultStyle = createDefaultStyle(workbook);

        int rowIndex = startIndex;
        for (Object[] row : rows) {
            Row excelRow = sheet.createRow(rowIndex);

            for (int colIndex = 0; colIndex < row.length; colIndex++) {
                org.apache.poi.ss.usermodel.Cell cell = excelRow.createCell(colIndex);
                Object value = row[colIndex];

                if (value == null) {
                    cell.setCellStyle(defaultStyle);
                    continue;
                }

                if (value instanceof BigDecimal) {
                    cell.setCellValue(((BigDecimal) value).doubleValue());
                    cell.setCellStyle(currencyStyle);
                } else if (value instanceof Double) {
                    cell.setCellValue((Double) value);
                    cell.setCellStyle(currencyStyle);
                } else if (value instanceof Float) {
                    cell.setCellValue(((Float) value).doubleValue());
                    cell.setCellStyle(currencyStyle);
                } else if (value instanceof Integer) {
                    cell.setCellValue((Integer) value);
                    cell.setCellStyle(integerStyle);
                } else if (value instanceof Long) {
                    cell.setCellValue((Long) value);
                    cell.setCellStyle(integerStyle);
                } else if (value instanceof LocalDate) {
                    cell.setCellValue(((LocalDate) value).format(DATE_FORMATTER));
                    cell.setCellStyle(dateStyle);
                } else if (value instanceof LocalDateTime) {
                    cell.setCellValue(((LocalDateTime) value).format(DATETIME_FORMATTER));
                    cell.setCellStyle(dateTimeStyle);
                } else if (value instanceof LocalTime) {
                    cell.setCellValue(((LocalTime) value).format(TIME_FORMATTER));
                    cell.setCellStyle(timeStyle);
                } else if (value instanceof Boolean) {
                    cell.setCellValue((Boolean) value);
                    cell.setCellStyle(defaultStyle);
                } else if (value instanceof Enum) {
                    cell.setCellValue(((Enum<?>) value).name());
                    cell.setCellStyle(defaultStyle);
                } else {
                    cell.setCellValue(value.toString());
                    cell.setCellStyle(defaultStyle);
                }
            }
            rowIndex++;
        }
        return rowIndex;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style, BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle createFilterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
        font.setItalic(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        applyFill(style, IndexedColors.LEMON_CHIFFON);
        applyBorder(style, BorderStyle.THIN);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        applyBorder(style, BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook, DataFormat dataFormat) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(dataFormat.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        applyBorder(style, BorderStyle.THIN);
        return style;
    }

    private CellStyle createCellDateStyle(Workbook workbook, DataFormat dataFormat) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        applyBorder(style, BorderStyle.THIN);
        return style;
    }

    private CellStyle createCellDateTimeStyle(Workbook workbook, DataFormat dataFormat) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        applyBorder(style, BorderStyle.THIN);
        return style;
    }

    private CellStyle createTimeStyle(Workbook workbook, DataFormat dataFormat) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        applyBorder(style, BorderStyle.THIN);
        return style;
    }

    private CellStyle createIntegerStyle(Workbook workbook, DataFormat dataFormat) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(dataFormat.getFormat("#,##0"));
        style.setAlignment(HorizontalAlignment.CENTER);
        applyBorder(style, BorderStyle.THIN);
        return style;
    }

    private CellStyle createDefaultStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        applyBorder(style, BorderStyle.THIN);
        return style;
    }

    private void applyBorder(CellStyle style, BorderStyle borderStyle) {
        style.setBorderTop(borderStyle);
        style.setBorderBottom(borderStyle);
        style.setBorderLeft(borderStyle);
        style.setBorderRight(borderStyle);
    }

    private void applyFill(CellStyle style, IndexedColors color) {
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(currentWidth, 6000));
        }
    }
}
