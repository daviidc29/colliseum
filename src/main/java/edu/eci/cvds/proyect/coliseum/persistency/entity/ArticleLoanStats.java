package edu.eci.cvds.proyect.coliseum.persistency.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "article_stats_reports")
public class ArticleLoanStats {
    @Id
    private String id;

    private String title;
    private int totalArticles;
    private LocalDateTime generationDate;
    private String generatedBy;

    @Transient
    private List<Map<String, Object>> statistics;

    private byte[] pdfFile;
    private byte[] excelFile;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getTotalArticles() { return totalArticles; }
    public void setTotalArticles(int totalArticles) { this.totalArticles = totalArticles; }

    public LocalDateTime getGenerationDate() { return generationDate; }
    public void setGenerationDate(LocalDateTime generationDate) { this.generationDate = generationDate; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public List<Map<String, Object>> getStatistics() { return statistics; }
    public void setStatistics(List<Map<String, Object>> statistics) { this.statistics = statistics; }

    public byte[] getPdfFile() { return pdfFile; }
    public void setPdfFile(byte[] pdfFile) { this.pdfFile = pdfFile; }

    public byte[] getExcelFile() { return excelFile; }
    public void setExcelFile(byte[] excelFile) { this.excelFile = excelFile; }
}