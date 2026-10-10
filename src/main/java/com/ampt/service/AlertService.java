package com.ampt.service;

import com.ampt.dto.AlertItemDto;
import com.ampt.entity.Market;
import com.ampt.entity.PriceRecord;
import com.ampt.repository.MarketRepository;
import com.ampt.repository.PriceRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertService {

    private final PriceRecordRepository priceRecordRepository;
    private final MarketRepository marketRepository;

    @Value("${ampt.alert.threshold-percent:15}")
    private double thresholdPercent;

    @Value("${ampt.alert.cutoff-hour:17}")
    private int cutoffHour;

    public AlertService(PriceRecordRepository priceRecordRepository, MarketRepository marketRepository) {
        this.priceRecordRepository = priceRecordRepository;
        this.marketRepository = marketRepository;
    }

    public double getThresholdPercent() { return thresholdPercent; }
    public int getCutoffHour() { return cutoffHour; }

    /**
     * B13: Evaluate whether a price movement breaches configured threshold (e.g. >= 15%).
     */
    public boolean checkAndApplyThresholdRule(PriceRecord record) {
        if (record == null || record.getMarket() == null || record.getCommodity() == null) {
            return false;
        }

        Optional<PriceRecord> priorOpt = priceRecordRepository
                .findTopByMarket_IdAndCommodity_IdAndRecordDateLessThanOrderByRecordDateDesc(
                        record.getMarket().getId(),
                        record.getCommodity().getId(),
                        record.getRecordDate());

        if (priorOpt.isPresent()) {
            BigDecimal priorModal = priorOpt.get().getModalPrice();
            if (priorModal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal diff = record.getModalPrice().subtract(priorModal);
                BigDecimal pct = diff.multiply(BigDecimal.valueOf(100))
                        .divide(priorModal, 2, RoundingMode.HALF_UP);

                if (pct.abs().doubleValue() >= thresholdPercent) {
                    record.setAlert(true);
                    return true;
                }
            }
        }

        record.setAlert(false);
        return false;
    }

    /**
     * B13 + B15: Retrieve all active threshold spikes for a given date or all active records.
     */
    @Transactional(readOnly = true)
    public List<AlertItemDto> getThresholdBreachAlerts(LocalDate targetDate) {
        List<PriceRecord> records = (targetDate != null) ?
                priceRecordRepository.findByRecordDate(targetDate) :
                priceRecordRepository.findAll();

        List<AlertItemDto> alerts = new ArrayList<>();

        for (PriceRecord r : records) {
            Optional<PriceRecord> priorOpt = priceRecordRepository
                    .findTopByMarket_IdAndCommodity_IdAndRecordDateLessThanOrderByRecordDateDesc(
                            r.getMarket().getId(),
                            r.getCommodity().getId(),
                            r.getRecordDate());

            if (priorOpt.isPresent()) {
                BigDecimal priorModal = priorOpt.get().getModalPrice();
                if (priorModal.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal diff = r.getModalPrice().subtract(priorModal);
                    BigDecimal pct = diff.multiply(BigDecimal.valueOf(100))
                            .divide(priorModal, 2, RoundingMode.HALF_UP);

                    if (pct.abs().doubleValue() >= thresholdPercent) {
                        String direction = pct.compareTo(BigDecimal.ZERO) > 0 ? "Spike (+)" : "Drop (-)";
                        String severity = pct.abs().doubleValue() >= (thresholdPercent * 1.5) ? "HIGH" : "MEDIUM";
                        String msg = String.format("Modal price moved by %s%.2f%% from ₹%.2f to ₹%.2f (Threshold: %.1f%%)",
                                pct.compareTo(BigDecimal.ZERO) > 0 ? "+" : "",
                                pct.doubleValue(), priorModal.doubleValue(), r.getModalPrice().doubleValue(), thresholdPercent);

                        alerts.add(new AlertItemDto(
                                "THRESHOLD_BREACH",
                                r.getCommodity().getName() + " " + direction + " at " + r.getMarket().getName(),
                                r.getMarket().getName(),
                                r.getCommodity().getName(),
                                r.getModalPrice(),
                                priorModal,
                                pct,
                                BigDecimal.valueOf(thresholdPercent),
                                r.getRecordDate(),
                                severity,
                                msg
                        ));
                    }
                }
            }
        }
        return alerts;
    }

    /**
     * B14: Rule flagging markets that are missing today's price filing after the cutoff time.
     */
    @Transactional(readOnly = true)
    public List<AlertItemDto> getMissingDataAlerts(LocalDate targetDate) {
        LocalDate date = (targetDate != null) ? targetDate :
                priceRecordRepository.findTopByOrderByRecordDateDesc()
                        .map(PriceRecord::getRecordDate)
                        .orElse(LocalDate.now());

        List<Market> allMarkets = marketRepository.findAll();
        List<PriceRecord> dayRecords = priceRecordRepository.findByRecordDate(date);
        Set<Long> reportingMarketIds = dayRecords.stream()
                .map(r -> r.getMarket().getId())
                .collect(Collectors.toSet());

        List<AlertItemDto> missingAlerts = new ArrayList<>();

        for (Market m : allMarkets) {
            if (!reportingMarketIds.contains(m.getId())) {
                missingAlerts.add(new AlertItemDto(
                        "MISSING_DATA",
                        "Missing Daily Filing: " + m.getName(),
                        m.getName(),
                        "All Commodities",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(thresholdPercent),
                        date,
                        "HIGH",
                        String.format("Mandi has not submitted arrival or price records for %s (Daily cutoff: %02d:00 hrs).",
                                date, cutoffHour)
                ));
            }
        }

        return missingAlerts;
    }

    /**
     * B15: Combined alerts/exceptions list view.
     */
    @Transactional(readOnly = true)
    public List<AlertItemDto> getAllActiveAlerts(LocalDate targetDate) {
        List<AlertItemDto> combined = new ArrayList<>();
        combined.addAll(getThresholdBreachAlerts(targetDate));
        combined.addAll(getMissingDataAlerts(targetDate));
        return combined;
    }
}
