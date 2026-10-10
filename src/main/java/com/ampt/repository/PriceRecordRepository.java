package com.ampt.repository;

import com.ampt.entity.PriceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PriceRecordRepository extends JpaRepository<PriceRecord, Long>, JpaSpecificationExecutor<PriceRecord> {

    // B06: search by market/commodity/date range (any combination may be null -> see service layer for query building)
    Page<PriceRecord> findByMarket_IdAndCommodity_IdAndRecordDateBetween(
            Long marketId, Long commodityId, LocalDate from, LocalDate to, Pageable pageable);

    // B12: full history for one market+commodity, newest first
    List<PriceRecord> findByMarket_IdAndCommodity_IdOrderByRecordDateDesc(Long marketId, Long commodityId);

    // B08/B09: today's records for a commodity, across all markets
    List<PriceRecord> findByCommodity_IdAndRecordDate(Long commodityId, LocalDate date);

    // B09: most recent prior record for a market+commodity, to compute % change
    Optional<PriceRecord> findTopByMarket_IdAndCommodity_IdAndRecordDateLessThanOrderByRecordDateDesc(
            Long marketId, Long commodityId, LocalDate beforeDate);

    // B09: most recent prior record for a commodity across all markets
    Optional<PriceRecord> findTopByCommodity_IdAndRecordDateLessThanOrderByRecordDateDesc(
            Long commodityId, LocalDate beforeDate);

    // Latest available reporting date in database
    Optional<PriceRecord> findTopByOrderByRecordDateDesc();

    // B15: active alerts, most recent first
    List<PriceRecord> findByAlertTrueOrderByRecordDateDesc();
}
