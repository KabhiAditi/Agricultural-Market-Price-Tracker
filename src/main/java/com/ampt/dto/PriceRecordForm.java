package com.ampt.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for creating and editing Price Records (B03/B04).
 */
public class PriceRecordForm {

    private Long id;

    @NotNull(message = "Market selection is required.")
    private Long marketId;

    @NotNull(message = "Commodity selection is required.")
    private Long commodityId;

    @NotNull(message = "Reporting date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate recordDate;

    @NotNull(message = "Minimum price is required.")
    @DecimalMin(value = "0.01", message = "Minimum price must be greater than zero.")
    private BigDecimal minPrice;

    @NotNull(message = "Maximum price is required.")
    @DecimalMin(value = "0.01", message = "Maximum price must be greater than zero.")
    private BigDecimal maxPrice;

    @NotNull(message = "Modal price is required.")
    @DecimalMin(value = "0.01", message = "Modal price must be greater than zero.")
    private BigDecimal modalPrice;

    @NotNull(message = "Arrival quantity is required.")
    @DecimalMin(value = "0.00", message = "Arrival quantity must be zero or positive.")
    private BigDecimal arrivalQty;

    public PriceRecordForm() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMarketId() { return marketId; }
    public void setMarketId(Long marketId) { this.marketId = marketId; }

    public Long getCommodityId() { return commodityId; }
    public void setCommodityId(Long commodityId) { this.commodityId = commodityId; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public BigDecimal getModalPrice() { return modalPrice; }
    public void setModalPrice(BigDecimal modalPrice) { this.modalPrice = modalPrice; }

    public BigDecimal getArrivalQty() { return arrivalQty; }
    public void setArrivalQty(BigDecimal arrivalQty) { this.arrivalQty = arrivalQty; }
}
