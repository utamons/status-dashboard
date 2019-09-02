package com.corn.data.entity;

import com.corn.data.dto.IssueReportDTO;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

import static javax.persistence.GenerationType.SEQUENCE;

@SuppressWarnings("unused")
@Entity
@Table(name = "ISSUE_REPORT")
public class IssueReport {
    private Long id;
    private String reportText;
    private String remarkText;
    private Instant createdAt;
    private boolean processed;
    private Instant processedAt;
    private String processedBy;

    public IssueReport() {
    }

    public IssueReport(String reportText, Instant createdAt) {
        this.reportText = reportText;
        this.createdAt = createdAt;
        this.processed = false;
    }

    @Id
    @SequenceGenerator(name = "ISSUE_REPORT_SEQ", sequenceName = "SEQ_ISSUE_REPORT", allocationSize=1)
    @GeneratedValue(strategy = SEQUENCE, generator = "ISSUE_REPORT_SEQ")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "REPORT_TEXT", nullable = false)
    public String getReportText() {
        return reportText;
    }

    public void setReportText(String reportText) {
        this.reportText = reportText;
    }

    @Column(name = "REMARK_TEXT")
    public String getRemarkText() {
        return remarkText;
    }

    public void setRemarkText(String remarkText) {
        this.remarkText = remarkText;
    }

    @Column(name = "CREATED_AT", nullable = false)
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Column(name = "PROCESSED", nullable = false)
    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    @Column(name = "PROCESSED_AT")
    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    @Column(name = "PROCESSED_BY")
    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IssueReport that = (IssueReport) o;
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

    @Transient
    public IssueReportDTO toValue() {
        return new IssueReportDTO(id,reportText,remarkText,createdAt,processed,processedAt,processedBy);
    }
}
