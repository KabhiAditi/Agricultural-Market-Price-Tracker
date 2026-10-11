package com.ampt.service;

import com.ampt.dto.CommoditySummaryDto;
import com.ampt.entity.Commodity;
import com.ampt.entity.PriceRecord;
import com.ampt.repository.CommodityRepository;
import com.ampt.repository.PriceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SummaryService {

    private final PriceRecordRepository priceRecordRepository;
    private final CommodityRepository commodityRepository;

    public SummaryService(PriceRecordRepository priceRecordRepository, CommodityRepository commodityRepository) {
        this.priceRecordRepository = priceRecordRepository;
        this.commodityRepository = commodityRepository;
    }

    /**
     * B08 + B09 + B10: Computes daily average/modal price, % change vs previous reporting day,
     * with graceful "no prior data" handling.
     */
    public List<CommoditySummaryDto> getDailySummaries(LocalDate requestDate) {
        LocalDate targetDate = requestDate;
        if (targetDate == null) {
            targetDate = priceRecordRepository.findTopByOrderByRecordDateDesc()
                    .map(PriceRecord::getRecordDate)
                    .orElse(LocalDate.now());
        }

        List<Commodity> commodities = commodityRepository.findAll();
        List<CommoditySummaryDto> result = new ArrayList<>();

        for (Commodity c : commodities) {
            CommoditySummaryDto dto = new CommoditySummaryDto();
            dto.setCommodityId(c.getId());
            dto.setCommodityName(c.getName());
            dto.setUnit(c.getUnit());
            dto.setReportDate(targetDate);

            List<PriceRecord> records = priceRecordRepository.findByCommodity_IdAndRecordDate(c.getId(), targetDate);

            if (records.isEmpty()) {
                dto.setReportingMarketsCount(0);
                dto.setTotalArrivalQty(BigDecimal.ZERO);
                dto.setHasPriorData(false);
                result.add(dto);
                continue;
            }

            dto.setReportingMarketsCount(records.size());

            BigDecimal minPrice = records.stream()
                    .map(PriceRecord::getMinPrice)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            BigDecimal maxPrice = records.stream()
                    .map(PriceRecord::getMaxPrice)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            BigDecimal totalModal = records.stream()
                    .map(PriceRecord::getModalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalArrival = records.stream()
                    .map(r -> r.getArrivalQty() != null ? r.getArrivalQty() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal count = BigDecimal.valueOf(records.size());
            BigDecimal avgModal = totalModal.divide(count, 2, RoundingMode.HALF_UP);

            dto.setMinPrice(minPrice);
            dto.setMaxPrice(maxPrice);
            dto.setModalPrice(avgModal);
            dto.setAvgPrice(avgModal);
            dto.setTotalArrivalQty(totalArrival);

            // B09: Compute % change vs previous reporting day
            Optional<PriceRecord> prevRecordOpt = priceRecordRepository
                    .findTopByCommodity_IdAndRecordDateLessThanOrderByRecordDateDesc(c.getId(), targetDate);

            if (prevRecordOpt.isPresent()) {
                LocalDate prevDate = prevRecordOpt.get().getRecordDate();
                List<PriceRecord> prevRecords = priceRecordRepository.findByCommodity_IdAndRecordDate(c.getId(), prevDate);

                if (!prevRecords.isEmpty()) {
                    BigDecimal prevTotal = prevRecords.stream()
                            .map(PriceRecord::getModalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal prevAvg = prevTotal.divide(BigDecimal.valueOf(prevRecords.size()), 2, RoundingMode.HALF_UP);

                    dto.setPrevDayModalPrice(prevAvg);

                    if (prevAvg.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal diff = avgModal.subtract(prevAvg);
                        // INTENTIONAL DEFECT DEMO: Divides by today's avgModal instead of yesterday's prevAvg
                        BigDecimal pct = diff.multiply(BigDecimal.valueOf(100))
                                .divide(avgModal, 2, RoundingMode.HALF_UP);
                        dto.setPercentChange(pct);
                        dto.setHasPriorData(true);
                    } else {
                        dto.setHasPriorData(false);
                    }
                } else {
                    dto.setHasPriorData(false);
                }
            } else {
                // Explicit "No prior data" condition
                dto.setHasPriorData(false);
                dto.setPercentChange(null);
            }

            result.add(dto);
        }

        return result;
    }

    /**
     * B12: Retrieve chronological price history for single market+commodity.
     */
    public List<PriceRecord> getMarketCommodityHistory(Long marketId, Long commodityId) {
        if (marketId != null && commodityId != null) {
            return priceRecordRepository.findByMarket_IdAndCommodity_IdOrderByRecordDateDesc(marketId, commodityId);
        }
        return List.of();
    }
}
