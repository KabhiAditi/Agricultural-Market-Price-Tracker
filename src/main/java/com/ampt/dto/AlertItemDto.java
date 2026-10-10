package com.ampt.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO representing an alert or exception (B13, B14, B15).
 */
public class AlertItemDto {

    private String type; // THRESHOLD_BREACH or MISSING_DATA
    private String title;
    private String marketName;
    private String commodityName;
    private BigDecimal currentPrice;
    private BigDecimal previousPrice;
    private BigDecimal percentChange;
    private BigDecimal thresholdPercent;
    private LocalDate alertDate;
    private String severity; // HIGH, MEDIUM, INFO
    private String message;

    public AlertItemDto() {}

    public AlertItemDto(String type, String title, String marketName, String commodityName,
                        BigDecimal currentPrice, BigDecimal previousPrice, BigDecimal percentChange,
                        BigDecimal thresholdPercent, LocalDate alertDate, String severity, String message) {
        this.type = type;
        this.title = title;
        this.marketName = marketName;
        this.commodityName = commodityName;
        this.currentPrice = currentPrice;
        this.previousPrice = previousPrice;
        this.percentChange = percentChange;
        this.thresholdPercent = thresholdPercent;
        this.alertDate = alertDate;
        this.severity = severity;
        this.message = message;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMarketName() { return marketName; }
    public void setMarketName(String marketName) { this.marketName = marketName; }

    public String getCommodityName() { return commodityName; }
    public void setCommodityName(String commodityName) { this.commodityName = commodityName; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getPreviousPrice() { return previousPrice; }
    public void setPreviousPrice(BigDecimal previousPrice) { this.previousPrice = previousPrice; }

    public BigDecimal getPercentChange() { return percentChange; }
    public void setPercentChange(BigDecimal percentChange) { this.percentChange = percentChange; }

    public BigDecimal getThresholdPercent() { return thresholdPercent; }
    public void setThresholdPercent(BigDecimal thresholdPercent) { this.thresholdPercent = thresholdPercent; }

    public LocalDate getAlertDate() { return alertDate; }
    public void setAlertDate(LocalDate alertDate) { this.alertDate = alertDate; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
