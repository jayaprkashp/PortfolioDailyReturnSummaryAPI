package com.portfolio.dailyreturn.summaryapi.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Model to hold the Portfolio response details
 */

public class PortfolioResponse {
	
	private String portfolioId;
    private LocalDate valuationDate;

    private BigDecimal portfolioReturnPct;
    private BigDecimal benchmarkReturnPct;
    private BigDecimal excessReturnPct;

    private String status;

    private List<String> reasons;

    private LocalDateTime processedAt;

    // Getters and setters

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    public BigDecimal getPortfolioReturnPct() {
        return portfolioReturnPct;
    }

    public void setPortfolioReturnPct(BigDecimal portfolioReturnPct) {
        this.portfolioReturnPct = portfolioReturnPct;
    }

    public BigDecimal getBenchmarkReturnPct() {
        return benchmarkReturnPct;
    }

    public void setBenchmarkReturnPct(BigDecimal benchmarkReturnPct) {
        this.benchmarkReturnPct = benchmarkReturnPct;
    }

    public BigDecimal getExcessReturnPct() {
        return excessReturnPct;
    }

    public void setExcessReturnPct(BigDecimal excessReturnPct) {
        this.excessReturnPct = excessReturnPct;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}