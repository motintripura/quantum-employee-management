package com.employee.employee_management.service.export;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    private static final DeviceRgb HEADER_BG_COLOR =
            new DeviceRgb(41, 65, 122);

    private static final DeviceRgb LIGHT_GRAY =
            new DeviceRgb(240, 240, 240);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /**
     * Generate PDF report
     */
    public byte[] generatePdf(
            String title,
            List<String> headers,
            List<Object[]> rows,
            String filterSummary
    ) {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(baos);

            PdfDocument pdfDoc = new PdfDocument(writer);

            // Landscape A4
            pdfDoc.setDefaultPageSize(PageSize.A4.rotate());

            /*
             * Create document
             *
             * Bottom margin is slightly larger because
             * page number footer is placed at the bottom.
             */
            Document document = new Document(pdfDoc);

            document.setMargins(
                    30, // top
                    30, // right
                    45, // bottom
                    30  // left
            );

            // Default fonts
            PdfFont font = PdfFontFactory.createFont(
                    StandardFonts.HELVETICA
            );

            PdfFont boldFont = PdfFontFactory.createFont(
                    StandardFonts.HELVETICA_BOLD
            );

            /*
             * Add page number footer
             */
            addPageNumberFooter(pdfDoc);

            /*
             * Report content
             */
            addCompanyHeader(document, font);

            addReportTitle(
                    document,
                    title,
                    boldFont
            );

            addGeneratedDate(
                    document,
                    font
            );

            addFilterSummary(
                    document,
                    filterSummary,
                    font
            );

            addDataTable(
                    document,
                    headers,
                    rows,
                    boldFont,
                    font
            );

            /*
             * Close document
             */
            document.close();

            return baos.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate PDF",
                    e
            );
        }
    }

    /**
     * Company header
     */
    private void addCompanyHeader(
            Document document,
            PdfFont font
    ) {

        Paragraph companyHeader =
                new Paragraph("EMPLOYEE MANAGEMENT SYSTEM")
                        .setFont(font)
                        .setFontSize(18)
                        .setBold()
                        .setFontColor(HEADER_BG_COLOR)
                        .setTextAlignment(
                                TextAlignment.CENTER
                        )
                        .setMarginBottom(5);

        document.add(companyHeader);
    }

    /**
     * Report title
     */
    private void addReportTitle(
            Document document,
            String title,
            PdfFont font
    ) {

        Paragraph reportTitle =
                new Paragraph(
                        title != null ? title : "Report"
                )
                        .setFont(font)
                        .setFontSize(14)
                        .setBold()
                        .setTextAlignment(
                                TextAlignment.CENTER
                        )
                        .setMarginBottom(5);

        document.add(reportTitle);
    }

    /**
     * Generated date
     */
    private void addGeneratedDate(
            Document document,
            PdfFont font
    ) {

        String generatedDate =
                "Generated: "
                        + LocalDateTime.now()
                        .format(DATE_FORMATTER);

        Paragraph dateParagraph =
                new Paragraph(generatedDate)
                        .setFont(font)
                        .setFontSize(9)
                        .setFontColor(
                                ColorConstants.GRAY
                        )
                        .setTextAlignment(
                                TextAlignment.CENTER
                        )
                        .setMarginBottom(10);

        document.add(dateParagraph);
    }

    /**
     * Filter summary
     */
    private void addFilterSummary(
            Document document,
            String filterSummary,
            PdfFont font
    ) {

        if (
                filterSummary == null
                        || filterSummary.isBlank()
        ) {
            return;
        }

        Paragraph filterParagraph =
                new Paragraph()
                        .setFont(font)
                        .setFontSize(9)
                        .setMarginBottom(10);

        Paragraph filterTitle =
                new Paragraph("Filters Applied:")
                        .setFont(font)
                        .setFontSize(9)
                        .setBold()
                        .setMarginBottom(2);

        Paragraph filterValue =
                new Paragraph(filterSummary)
                        .setFont(font)
                        .setFontSize(8)
                        .setFontColor(
                                ColorConstants.DARK_GRAY
                        );

        filterParagraph.add(filterTitle);
        filterParagraph.add(filterValue);

        document.add(filterParagraph);
    }

    /**
     * Add data table
     */
    private void addDataTable(
            Document document,
            List<String> headers,
            List<Object[]> rows,
            PdfFont boldFont,
            PdfFont font
    ) {

        /*
         * No headers
         */
        if (
                headers == null
                        || headers.isEmpty()
        ) {

            document.add(
                    new Paragraph("No data available")
                            .setFont(font)
            );

            return;
        }

        /*
         * Calculate equal column widths
         */
        float[] columnWidths =
                new float[headers.size()];

        float widthPerColumn =
                100f / headers.size();

        for (int i = 0;
             i < headers.size();
             i++) {

            columnWidths[i] =
                    widthPerColumn;
        }

        /*
         * Create table
         */
        Table table =
                new Table(
                        UnitValue.createPercentArray(
                                columnWidths
                        )
                )
                        .useAllAvailableWidth()
                        .setHorizontalAlignment(
                                HorizontalAlignment.CENTER
                        );

        /*
         * Header
         */
        addHeaderRow(
                table,
                headers,
                boldFont
        );

        /*
         * Data
         */
        addDataRows(
                table,
                rows,
                font
        );

        document.add(table);
    }

    /**
     * Add table header row
     */
    private void addHeaderRow(
            Table table,
            List<String> headers,
            PdfFont font
    ) {

        for (String header : headers) {

            String headerText =
                    header != null
                            ? header
                            : "";

            Cell cell =
                    new Cell()
                            .add(
                                    new Paragraph(
                                            headerText
                                    )
                                            .setFont(font)
                                            .setFontSize(8)
                                            .setFontColor(
                                                    ColorConstants.WHITE
                                            )
                                            .setBold()
                            )
                            .setBackgroundColor(
                                    HEADER_BG_COLOR
                            )
                            .setPadding(6)
                            .setBorder(
                                    new SolidBorder(
                                            ColorConstants.WHITE,
                                            0.5f
                                    )
                            )
                            .setTextAlignment(
                                    TextAlignment.CENTER
                            );

            table.addHeaderCell(cell);
        }
    }

    /**
     * Add table data rows
     */
    private void addDataRows(
            Table table,
            List<Object[]> rows,
            PdfFont font
    ) {

        if (rows == null || rows.isEmpty()) {

            /*
             * Add one row indicating no data
             */
            Cell cell =
                    new Cell(1, 1)
                            .add(
                                    new Paragraph(
                                            "No data available"
                                    )
                                            .setFont(font)
                                            .setFontSize(8)
                            )
                            .setTextAlignment(
                                    TextAlignment.CENTER
                            );

            table.addCell(cell);

            return;
        }

        for (
                int rowIndex = 0;
                rowIndex < rows.size();
                rowIndex++
        ) {

            Object[] row =
                    rows.get(rowIndex);

            if (row == null) {
                continue;
            }

            for (
                    Object cellData : row
            ) {

                String cellValue =
                        cellData != null
                                ? cellData.toString()
                                : "";

                Cell cell =
                        new Cell()
                                .add(
                                        new Paragraph(
                                                cellValue
                                        )
                                                .setFont(font)
                                                .setFontSize(7)
                                )
                                .setPadding(4)
                                .setBorder(
                                        new SolidBorder(
                                                ColorConstants.LIGHT_GRAY,
                                                0.5f
                                        )
                                )
                                .setTextAlignment(
                                        TextAlignment.LEFT
                                );

                /*
                 * Alternate row background
                 */
                if (rowIndex % 2 == 1) {

                    cell.setBackgroundColor(
                            LIGHT_GRAY
                    );
                }

                table.addCell(cell);
            }
        }
    }

    /**
     * Add page number footer
     *
     * iText 8 does not have HeaderFooter
     * in com.itextpdf.layout.element.
     *
     * Therefore, PdfDocumentEvent.END_PAGE
     * is used for footer rendering.
     */
    private void addPageNumberFooter(
            PdfDocument pdfDoc
    ) {

        pdfDoc.addEventHandler(
                PdfDocumentEvent.END_PAGE,
                new IEventHandler() {

                    @Override
                    public void handleEvent(
                            Event event
                    ) {

                        PdfDocumentEvent docEvent =
                                (PdfDocumentEvent) event;

                        PdfPage page =
                                docEvent.getPage();

                        Rectangle pageSize =
                                page.getPageSize();

                        float left =
                                pageSize.getLeft();

                        float right =
                                pageSize.getRight();

                        float bottom =
                                pageSize.getBottom();

                        /*
                         * Footer positions
                         */
                        float lineY =
                                bottom + 28;

                        float textY =
                                bottom + 15;

                        /*
                         * Create canvas
                         */
                        PdfCanvas canvas =
                                new PdfCanvas(page);

                        try {

                            /*
                             * Footer font
                             */
                            PdfFont footerFont =
                                    PdfFontFactory.createFont(
                                            StandardFonts.HELVETICA
                                    );

                            /*
                             * Footer top border
                             */
                            canvas.setStrokeColor(
                                    ColorConstants.LIGHT_GRAY
                            );

                            canvas.setLineWidth(
                                    0.5f
                            );

                            canvas.moveTo(
                                    left + 30,
                                    lineY
                            );

                            canvas.lineTo(
                                    right - 30,
                                    lineY
                            );

                            canvas.stroke();

                            /*
                             * Page number
                             */
                            String pageText =
                                    "Page "
                                            + pdfDoc.getPageNumber(
                                                    page
                                            );

                            /*
                             * Calculate text width
                             * for center alignment
                             */
                            float textWidth =
                                    footerFont.getWidth(
                                            pageText,
                                            8
                                    );

                            float pageWidth =
                                    pageSize.getWidth();

                            float textX =
                                    left
                                            + (
                                            pageWidth
                                                    - textWidth
                                    ) / 2;

                            /*
                             * Draw page number
                             */
                            canvas.beginText();

                            canvas.setFontAndSize(
                                    footerFont,
                                    8
                            );

                            canvas.moveText(
                                    textX,
                                    textY
                            );

                            canvas.showText(
                                    pageText
                            );

                            canvas.endText();

                        } catch (IOException e) {

                            throw new RuntimeException(
                                    "Failed to create PDF footer",
                                    e
                            );
                        }
                    }
                }
        );
    }
}