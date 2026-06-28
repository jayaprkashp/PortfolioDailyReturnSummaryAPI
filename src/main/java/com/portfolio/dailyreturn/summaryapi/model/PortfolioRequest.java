package com.portfolio.dailyreturn.summaryapi.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PortfolioRequest {
	
    private String portfolioId;
    private LocalDate valuationDate;
    private BigDecimal beginMarketValue;
    private BigDecimal endMarketValue;
    private BigDecimal netCashFlow;
    private BigDecimal benchmarkReturnPct;
    private String currency;
    private String requestedBy;

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

    public BigDecimal getBeginMarketValue() {
        return beginMarketValue;
    }

    public void setBeginMarketValue(BigDecimal beginMarketValue) {
        this.beginMarketValue = beginMarketValue;
    }

    public BigDecimal getEndMarketValue() {
        return endMarketValue;
    }

    public void setEndMarketValue(BigDecimal endMarketValue) {
        this.endMarketValue = endMarketValue;
    }

    public BigDecimal getNetCashFlow() {
        return netCashFlow;
    }

    public void setNetCashFlow(BigDecimal netCashFlow) {
        this.netCashFlow = netCashFlow;
    }

    public BigDecimal getBenchmarkReturnPct() {
        return benchmarkReturnPct;
    }

    public void setBenchmarkReturnPct(BigDecimal benchmarkReturnPct) {
        this.benchmarkReturnPct = benchmarkReturnPct;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }
}
