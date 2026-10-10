package com.ampt.controller;

import com.ampt.dto.AlertItemDto;
import com.ampt.service.AlertService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * B15: Dedicated alerts/exceptions list view.
     */
    @GetMapping("/alerts")
    public String alertsPage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "all") String filter,
            Model model) {

        List<AlertItemDto> thresholdAlerts = alertService.getThresholdBreachAlerts(date);
        List<AlertItemDto> missingAlerts = alertService.getMissingDataAlerts(date);

        List<AlertItemDto> displayedAlerts;
        if ("threshold".equalsIgnoreCase(filter)) {
            displayedAlerts = thresholdAlerts;
        } else if ("missing".equalsIgnoreCase(filter)) {
            displayedAlerts = missingAlerts;
        } else {
            displayedAlerts = alertService.getAllActiveAlerts(date);
        }

        model.addAttribute("alerts", displayedAlerts);
        model.addAttribute("thresholdCount", thresholdAlerts.size());
        model.addAttribute("missingCount", missingAlerts.size());
        model.addAttribute("totalAlertsCount", thresholdAlerts.size() + missingAlerts.size());
        model.addAttribute("thresholdPercent", alertService.getThresholdPercent());
        model.addAttribute("cutoffHour", alertService.getCutoffHour());
        model.addAttribute("filter", filter);
        model.addAttribute("targetDate", date);

        return "alerts/index";
    }

    /**
     * REST endpoint returning alerts in JSON format for automated checks.
     */
    @GetMapping("/api/alerts")
    @ResponseBody
    public List<AlertItemDto> getAlertsApi(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return alertService.getAllActiveAlerts(date);
    }
}
