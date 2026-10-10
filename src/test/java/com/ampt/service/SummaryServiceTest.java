package com.ampt.service;

import com.ampt.dto.CommoditySummaryDto;
import com.ampt.entity.Commodity;
import com.ampt.entity.Market;
import com.ampt.entity.PriceRecord;
import com.ampt.repository.CommodityRepository;
import com.ampt.repository.PriceRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    @Mock
    private PriceRecordRepository priceRecordRepository;

    @Mock
    private CommodityRepository commodityRepository;

    @InjectMocks
    private SummaryService summaryService;

    private Commodity tomato;
    private Market nagpur;
    private Market pune;

    @BeforeEach
    void setUp() {
        tomato = new Commodity("Tomato", "quintal");
        tomato.setId(1L);

        nagpur = new Market("Nagpur APMC", "Nagpur", "Maharashtra");
        nagpur.setId(1L);

        pune = new Market("Pune APMC", "Pune", "Maharashtra");
        pune.setId(2L);
    }

    @Test
    @DisplayName("B08 + B09: Computes daily average modal price and % change vs previous day")
    void testDailySummaries_ComputesAvgAndPercentChange() {
        LocalDate today = LocalDate.of(2026, 10, 10);
        LocalDate yesterday = LocalDate.of(2026, 10, 9);

        // Today's records: Nagpur @ 2000, Pune @ 2200 -> Average = 2100.00
        PriceRecord today1 = new PriceRecord();
        today1.setMarket(nagpur);
        today1.setCommodity(tomato);
        today1.setRecordDate(today);
        today1.setMinPrice(new BigDecimal("1800.00"));
        today1.setMaxPrice(new BigDecimal("2300.00"));
        today1.setModalPrice(new BigDecimal("2000.00"));
        today1.setArrivalQty(new BigDecimal("100.00"));

        PriceRecord today2 = new PriceRecord();
        today2.setMarket(pune);
        today2.setCommodity(tomato);
        today2.setRecordDate(today);
        today2.setMinPrice(new BigDecimal("1900.00"));
        today2.setMaxPrice(new BigDecimal("2400.00"));
        today2.setModalPrice(new BigDecimal("2200.00"));
        today2.setArrivalQty(new BigDecimal("150.00"));

        // Yesterday's record: @ 2000.00 -> Change = (2100 - 2000) / 2000 * 100 = +5.00%
        PriceRecord prevRecord = new PriceRecord();
        prevRecord.setRecordDate(yesterday);
        prevRecord.setModalPrice(new BigDecimal("2000.00"));

        when(commodityRepository.findAll()).thenReturn(List.of(tomato));
        when(priceRecordRepository.findByCommodity_IdAndRecordDate(1L, today)).thenReturn(List.of(today1, today2));
        when(priceRecordRepository.findTopByCommodity_IdAndRecordDateLessThanOrderByRecordDateDesc(1L, today))
                .thenReturn(Optional.of(prevRecord));
        when(priceRecordRepository.findByCommodity_IdAndRecordDate(1L, yesterday)).thenReturn(List.of(prevRecord));

        List<CommoditySummaryDto> summaries = summaryService.getDailySummaries(today);

        assertEquals(1, summaries.size());
        CommoditySummaryDto dto = summaries.get(0);
        assertEquals("Tomato", dto.getCommodityName());
        assertEquals(new BigDecimal("2100.00"), dto.getAvgPrice());
        assertEquals(2, dto.getReportingMarketsCount());
        assertTrue(dto.isHasPriorData());
        assertEquals(new BigDecimal("5.00"), dto.getPercentChange());
    }

    @Test
    @DisplayName("B09: Handles 'No prior data' gracefully when previous day data is absent")
    void testDailySummaries_NoPriorData_HandledGracefully() {
        LocalDate today = LocalDate.of(2026, 10, 10);

        PriceRecord today1 = new PriceRecord();
        today1.setMarket(nagpur);
        today1.setCommodity(tomato);
        today1.setRecordDate(today);
        today1.setMinPrice(new BigDecimal("1800.00"));
        today1.setMaxPrice(new BigDecimal("2200.00"));
        today1.setModalPrice(new BigDecimal("2000.00"));

        when(commodityRepository.findAll()).thenReturn(List.of(tomato));
        when(priceRecordRepository.findByCommodity_IdAndRecordDate(1L, today)).thenReturn(List.of(today1));
        when(priceRecordRepository.findTopByCommodity_IdAndRecordDateLessThanOrderByRecordDateDesc(1L, today))
                .thenReturn(Optional.empty());

        List<CommoditySummaryDto> summaries = summaryService.getDailySummaries(today);

        assertEquals(1, summaries.size());
        CommoditySummaryDto dto = summaries.get(0);
        assertFalse(dto.isHasPriorData());
        assertNull(dto.getPercentChange());
    }

    @Test
    @DisplayName("B12: Retrieves chronological history for market and commodity")
    void testGetMarketCommodityHistory() {
        PriceRecord r1 = new PriceRecord();
        r1.setRecordDate(LocalDate.of(2026, 10, 10));
        PriceRecord r2 = new PriceRecord();
        r2.setRecordDate(LocalDate.of(2026, 10, 9));

        when(priceRecordRepository.findByMarket_IdAndCommodity_IdOrderByRecordDateDesc(1L, 1L))
                .thenReturn(List.of(r1, r2));

        List<PriceRecord> history = summaryService.getMarketCommodityHistory(1L, 1L);

        assertEquals(2, history.size());
    }
}
