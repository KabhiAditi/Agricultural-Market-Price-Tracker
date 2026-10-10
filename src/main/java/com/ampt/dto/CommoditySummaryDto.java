package com.ampt.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Summary Indicator DTO for B08, B09, B10 dashboard widgets.
 */
public class CommoditySummaryDto {

    private Long commodityId;
    private String commodityName;
    private String unit;
    private BigDecimal avgPrice;
    private BigDecimal modalPrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal prevDayModalPrice;
    private BigDecimal percentChange;
    private boolean hasPriorData;
    private int reportingMarketsCount;
    private BigDecimal totalArrivalQty;
    private LocalDate reportDate;

    public CommoditySummaryDto() {}

    public Long getCommodityId() { return commodityId; }
    public void setCommodityId(Long commodityId) { this.commodityId = commodityId; }

    public String getCommodityName() { return commodityName; }
    public void setCommodityName(String commodityName) { this.commodityName = commodityName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getAvgPrice() { return avgPrice; }
    public void setAvgPrice(BigDecimal avgPrice) { this.avgPrice = avgPrice; }

    public BigDecimal getModalPrice() { return modalPrice; }
    public void setModalPrice(BigDecimal modalPrice) { this.modalPrice = modalPrice; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public BigDecimal getPrevDayModalPrice() { return prevDayModalPrice; }
    public void setPrevDayModalPrice(BigDecimal prevDayModalPrice) { this.prevDayModalPrice = prevDayModalPrice; }

    public BigDecimal getPercentChange() { return percentChange; }
    public void setPercentChange(BigDecimal percentChange) { this.percentChange = percentChange; }

    public boolean isHasPriorData() { return hasPriorData; }
    public void setHasPriorData(boolean hasPriorData) { this.hasPriorData = hasPriorData; }

    public int getReportingMarketsCount() { return reportingMarketsCount; }
    public void setReportingMarketsCount(int reportingMarketsCount) { this.reportingMarketsCount = reportingMarketsCount; }

    public BigDecimal getTotalArrivalQty() { return totalArrivalQty; }
    public void setTotalArrivalQty(BigDecimal totalArrivalQty) { this.totalArrivalQty = totalArrivalQty; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
}
