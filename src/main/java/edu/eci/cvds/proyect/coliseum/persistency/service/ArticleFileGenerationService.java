package edu.eci.cvds.proyect.coliseum.persistency.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;


import edu.eci.cvds.proyect.coliseum.persistency.entity.ArticleLoanStats;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ArticleFileGenerationService {

    public byte[] generateArticleStatsPdf(ArticleLoanStats stats) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc);

        // Título y encabezados
        doc.add(new Paragraph("Reporte de Estadísticas de Préstamos de Artículos")
                .setFontSize(18).setBold());
        doc.add(new Paragraph("Generado por: " + stats.getGeneratedBy())
                .setFontSize(12));
        doc.add(new Paragraph("Fecha de generación: " +
                stats.getGenerationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setFontSize(12));
        doc.add(new Paragraph("Total de artículos: " + stats.getTotalArticles())
                .setFontSize(12));
        doc.add(new Paragraph("\n"));

        // Crear tabla para los datos
        Table table = new Table(5);
        table.addHeaderCell("ID");
        table.addHeaderCell("Nombre");
        table.addHeaderCell("Estado");
        table.addHeaderCell("Descripción");
        table.addHeaderCell("Veces Prestado");

        // Agregar filas con datos
        for (Map<String, Object> item : stats.getStatistics()) {
            table.addCell(String.valueOf(item.get("id")));
            table.addCell(String.valueOf(item.get("name")));
            table.addCell(String.valueOf(item.get("articleStatus")));
            table.addCell(String.valueOf(item.getOrDefault("description", "")));
            table.addCell(String.valueOf(item.get("vecesPrestado")));
        }

        doc.add(table);
        doc.close();

        return out.toByteArray();
    }

    public byte[] generateArticleStatsExcel(ArticleLoanStats stats) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Workbook workbook = new XSSFWorkbook();

        // Crear hoja y estilos
        Sheet sheet = workbook.createSheet("Estadísticas de Préstamos");
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Crear información general del reporte
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Reporte de Estadísticas de Préstamos de Artículos");

        Row infoRow1 = sheet.createRow(1);
        infoRow1.createCell(0).setCellValue("Generado por:");
        infoRow1.createCell(1).setCellValue(stats.getGeneratedBy());

        Row infoRow2 = sheet.createRow(2);
        infoRow2.createCell(0).setCellValue("Fecha de generación:");
        infoRow2.createCell(1).setCellValue(
                stats.getGenerationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        Row infoRow3 = sheet.createRow(3);
        infoRow3.createCell(0).setCellValue("Total de artículos:");
        infoRow3.createCell(1).setCellValue(stats.getTotalArticles());

        // Crear encabezados de la tabla
        Row headerRow = sheet.createRow(5);
        String[] headers = {"ID", "Nombre", "Estado", "Descripción", "Veces Prestado"};
        for (int i = 0; i < headers.length; i++) {
            // Eliminar el casting innecesario
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Agregar datos de los artículos
        int rowNum = 6;
        for (Map<String, Object> item : stats.getStatistics()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(String.valueOf(item.get("id")));
            row.createCell(1).setCellValue(String.valueOf(item.get("name")));
            row.createCell(2).setCellValue(String.valueOf(item.get("articleStatus")));
            row.createCell(3).setCellValue(String.valueOf(item.getOrDefault("description", "")));
            row.createCell(4).setCellValue(String.valueOf(item.get("vecesPrestado")));
        }

        // Auto-ajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }
}