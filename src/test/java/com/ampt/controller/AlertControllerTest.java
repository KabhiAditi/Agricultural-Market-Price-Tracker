package com.ampt.controller;

import com.ampt.dto.AlertItemDto;
import com.ampt.service.AlertService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlertService alertService;

    @Test
    @DisplayName("GET /alerts renders alerts list page with model attributes (B15)")
    void testAlertsPage() throws Exception {
        AlertItemDto alert = new AlertItemDto(
                "THRESHOLD_BREACH",
                "Tomato Spike at Nagpur APMC",
                "Nagpur APMC",
                "Tomato",
                new BigDecimal("2400"),
                new BigDecimal("2000"),
                new BigDecimal("20.00"),
                new BigDecimal("15.0"),
                LocalDate.now(),
                "HIGH",
                "Modal price moved by +20.00%"
        );

        when(alertService.getAllActiveAlerts(any())).thenReturn(List.of(alert));
        when(alertService.getThresholdBreachAlerts(any())).thenReturn(List.of(alert));
        when(alertService.getMissingDataAlerts(any())).thenReturn(List.of());
        when(alertService.getThresholdPercent()).thenReturn(15.0);
        when(alertService.getCutoffHour()).thenReturn(17);

        mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk())
                .andExpect(view().name("alerts/index"))
                .andExpect(model().attributeExists("alerts", "thresholdPercent", "cutoffHour"));
    }

    @Test
    @DisplayName("GET /api/alerts returns JSON list of active alerts")
    void testGetAlertsApi() throws Exception {
        AlertItemDto alert = new AlertItemDto(
                "THRESHOLD_BREACH",
                "Tomato Spike",
                "Nagpur APMC",
                "Tomato",
                new BigDecimal("2400"),
                new BigDecimal("2000"),
                new BigDecimal("20.00"),
                new BigDecimal("15.0"),
                LocalDate.now(),
                "HIGH",
                "Test message"
        );

        when(alertService.getAllActiveAlerts(any())).thenReturn(List.of(alert));

        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("THRESHOLD_BREACH"))
                .andExpect(jsonPath("$[0].commodityName").value("Tomato"));
    }
}
