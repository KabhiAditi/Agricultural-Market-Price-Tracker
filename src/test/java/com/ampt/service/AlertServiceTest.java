package com.ampt.service;

import com.ampt.dto.AlertItemDto;
import com.ampt.entity.Commodity;
import com.ampt.entity.Market;
import com.ampt.entity.PriceRecord;
import com.ampt.repository.MarketRepository;
import com.ampt.repository.PriceRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private PriceRecordRepository priceRecordRepository;

    @Mock
    private MarketRepository marketRepository;

    @InjectMocks
    private AlertService alertService;

    private Market market1;
    private Market market2;
    private Commodity tomato;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(alertService, "thresholdPercent", 15.0);
        ReflectionTestUtils.setField(alertService, "cutoffHour", 17);

        market1 = new Market("Nagpur APMC", "Nagpur", "Maharashtra");
        market1.setId(1L);

        market2 = new Market("Pune APMC", "Pune", "Maharashtra");
        market2.setId(2L);

        tomato = new Commodity("Tomato", "quintal");
        tomato.setId(10L);
    }

    private PriceRecord createRecord(Market market, Commodity commodity, LocalDate date,
                                     BigDecimal min, BigDecimal max, BigDecimal modal, BigDecimal qty) {
        PriceRecord r = new PriceRecord();
        r.setMarket(market);
        r.setCommodity(commodity);
        r.setRecordDate(date);
        r.setMinPrice(min);
        r.setMaxPrice(max);
        r.setModalPrice(modal);
        r.setArrivalQty(qty);
        return r;
    }

    @Test
    @DisplayName("B13: Record with +20% increase triggers threshold alert (threshold = 15%)")
    void testCheckAndApplyThresholdRule_Spike() {
        LocalDate today = LocalDate.of(2026, 10, 10);
        LocalDate yesterday = today.minusDays(1);

        PriceRecord prior = createRecord(market1, tomato, yesterday,
                new BigDecimal("1800"), new BigDecimal("2200"), new BigDecimal("2000"), new BigDecimal("100"));

        PriceRecord current = createRecord(market1, tomato, today,
                new BigDecimal("2200"), new BigDecimal("2600"), new BigDecimal("2400"), new BigDecimal("100"));

        when(priceRecordRepository.findTopByMarket_IdAndCommodity_IdAndRecordDateLessThanOrderByRecordDateDesc(
                1L, 10L, today)).thenReturn(Optional.of(prior));

        boolean isAlert = alertService.checkAndApplyThresholdRule(current);

        assertTrue(isAlert);
        assertTrue(current.isAlert());
    }

    @Test
    @DisplayName("B13: Record with +5% change does not trigger threshold alert")
    void testCheckAndApplyThresholdRule_NormalChange() {
        LocalDate today = LocalDate.of(2026, 10, 10);
        LocalDate yesterday = today.minusDays(1);

        PriceRecord prior = createRecord(market1, tomato, yesterday,
                new BigDecimal("1800"), new BigDecimal("2200"), new BigDecimal("2000"), new BigDecimal("100"));

        PriceRecord current = createRecord(market1, tomato, today,
                new BigDecimal("1900"), new BigDecimal("2300"), new BigDecimal("2100"), new BigDecimal("100"));

        when(priceRecordRepository.findTopByMarket_IdAndCommodity_IdAndRecordDateLessThanOrderByRecordDateDesc(
                1L, 10L, today)).thenReturn(Optional.of(prior));

        boolean isAlert = alertService.checkAndApplyThresholdRule(current);

        assertFalse(isAlert);
        assertFalse(current.isAlert());
    }

    @Test
    @DisplayName("B13: Record with sharp drop (-20%) triggers threshold alert")
    void testCheckAndApplyThresholdRule_Drop() {
        LocalDate today = LocalDate.of(2026, 10, 10);
        LocalDate yesterday = today.minusDays(1);

        PriceRecord prior = createRecord(market1, tomato, yesterday,
                new BigDecimal("1800"), new BigDecimal("2200"), new BigDecimal("2000"), new BigDecimal("100"));

        PriceRecord current = createRecord(market1, tomato, today,
                new BigDecimal("1400"), new BigDecimal("1800"), new BigDecimal("1600"), new BigDecimal("100"));

        when(priceRecordRepository.findTopByMarket_IdAndCommodity_IdAndRecordDateLessThanOrderByRecordDateDesc(
                1L, 10L, today)).thenReturn(Optional.of(prior));

        boolean isAlert = alertService.checkAndApplyThresholdRule(current);

        assertTrue(isAlert);
        assertTrue(current.isAlert());
    }

    @Test
    @DisplayName("B13: No prior record does not trigger threshold alert")
    void testCheckAndApplyThresholdRule_NoPriorRecord() {
        LocalDate today = LocalDate.of(2026, 10, 10);
        PriceRecord current = createRecord(market1, tomato, today,
                new BigDecimal("1800"), new BigDecimal("2200"), new BigDecimal("2000"), new BigDecimal("100"));

        when(priceRecordRepository.findTopByMarket_IdAndCommodity_IdAndRecordDateLessThanOrderByRecordDateDesc(
                1L, 10L, today)).thenReturn(Optional.empty());

        boolean isAlert = alertService.checkAndApplyThresholdRule(current);

        assertFalse(isAlert);
        assertFalse(current.isAlert());
    }

    @Test
    @DisplayName("B14: Missing data rule flags market when no filing exists for target date")
    void testGetMissingDataAlerts() {
        LocalDate targetDate = LocalDate.of(2026, 10, 10);

        when(marketRepository.findAll()).thenReturn(List.of(market1, market2));

        PriceRecord recordForMarket1 = createRecord(market1, tomato, targetDate,
                new BigDecimal("1800"), new BigDecimal("2200"), new BigDecimal("2000"), new BigDecimal("100"));

        when(priceRecordRepository.findByRecordDate(targetDate)).thenReturn(List.of(recordForMarket1));

        List<AlertItemDto> missingAlerts = alertService.getMissingDataAlerts(targetDate);

        assertEquals(1, missingAlerts.size());
        assertEquals("Pune APMC", missingAlerts.get(0).getMarketName());
        assertEquals("MISSING_DATA", missingAlerts.get(0).getType());
    }

    @Test
    @DisplayName("B15: Active alerts list aggregates both threshold breaches and missing data")
    void testGetAllActiveAlerts() {
        LocalDate targetDate = LocalDate.of(2026, 10, 10);

        PriceRecord spikeRecord = createRecord(market1, tomato, targetDate,
                new BigDecimal("2200"), new BigDecimal("2600"), new BigDecimal("2400"), new BigDecimal("100"));

        PriceRecord prior = createRecord(market1, tomato, targetDate.minusDays(1),
                new BigDecimal("1800"), new BigDecimal("2200"), new BigDecimal("2000"), new BigDecimal("100"));

        when(priceRecordRepository.findByRecordDate(targetDate)).thenReturn(List.of(spikeRecord));
        when(priceRecordRepository.findTopByMarket_IdAndCommodity_IdAndRecordDateLessThanOrderByRecordDateDesc(
                1L, 10L, targetDate)).thenReturn(Optional.of(prior));
        when(marketRepository.findAll()).thenReturn(List.of(market1, market2));

        List<AlertItemDto> allAlerts = alertService.getAllActiveAlerts(targetDate);

        assertEquals(2, allAlerts.size());
        assertTrue(allAlerts.stream().anyMatch(a -> a.getType().equals("THRESHOLD_BREACH")));
        assertTrue(allAlerts.stream().anyMatch(a -> a.getType().equals("MISSING_DATA")));
    }
}
