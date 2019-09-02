package com.corn.data.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.corn.data.InstantDeserializer;
import com.corn.data.InstantSerializer;

import java.time.Instant;
import java.util.Objects;

@SuppressWarnings("unused")
public class IssueReportDTO {
    private Long id;
    private String reportText;
    private String remarkText;
    private Instant createdAt;
    private boolean processed;
    private Instant processedAt;
    private String processedBy;



    @JsonCreator
    public IssueReportDTO(
            @JsonProperty("id")
            Long id,
            @JsonProperty("reportText")
            String reportText,
            @JsonProperty("remarkText")
            String remarkText,
            @JsonProperty("createdAt")
            @JsonDeserialize(using = InstantDeserializer.class)
            Instant createdAt,
            @JsonProperty("processed")
            boolean processed,
            @JsonProperty("processedAt")
            @JsonDeserialize(using = InstantDeserializer.class)
            Instant processedAt,
            @JsonProperty("processedBy")
            String processedBy) {
        this.id = id;
        this.reportText = reportText;
        this.remarkText = remarkText;
        this.createdAt = createdAt;
        this.processed = processed;
        this.processedAt = processedAt;
        this.processedBy = processedBy;
    }

    public Long getId() {
        return id;
    }

    public String getReportText() {
        return reportText;
    }

    public String getRemarkText() {
        return remarkText;
    }

    @JsonSerialize(using = InstantSerializer.class)
    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isProcessed() {
        return processed;
    }

    @JsonSerialize(using = InstantSerializer.class)
    public Instant getProcessedAt() {
        return processedAt;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IssueReportDTO that = (IssueReportDTO) o;
        return processed == that.processed &&
                Objects.equals(id, that.id) &&
                Objects.equals(reportText, that.reportText) &&
                Objects.equals(remarkText, that.remarkText) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(processedAt, that.processedAt) &&
                Objects.equals(processedBy, that.processedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reportText, remarkText, createdAt, processed, processedAt, processedBy);
    }

    public IssueReportDTO withId(Long id) {
        return new IssueReportDTO(id, reportText, remarkText, createdAt, processed, processedAt, processedBy);
    }

    public IssueReportDTO withProcessed(boolean processed) {
        if (processed)
            return new IssueReportDTO(id, reportText, remarkText, createdAt, processed, processedAt, processedBy);
        else
            return new IssueReportDTO(id, reportText, remarkText, createdAt, processed, null, null);
    }

    public IssueReportDTO withReportText(String reportText) {
        return new IssueReportDTO(id, reportText, remarkText, createdAt, processed, processedAt, processedBy);
    }

    public IssueReportDTO withRemarkText(String remarkText) {
        return new IssueReportDTO(id, reportText, remarkText, createdAt, processed, processedAt, processedBy);
    }
}
