package com.ampt.controller;

import com.ampt.repository.CommodityRepository;
import com.ampt.repository.MarketRepository;
import com.ampt.repository.PriceRecordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Task 3 skeleton controller: proves the full stack (MVC -> service-less direct
 * repo call -> JPA -> H2) is wired correctly end to end. Real dashboard
 * aggregation (FR5/FR6) is implemented in Task 5/6 feature branches.
 */
@Controller
public class HomeController {

    private final MarketRepository marketRepository;
    private final CommodityRepository commodityRepository;
    private final PriceRecordRepository priceRecordRepository;
    private final com.ampt.service.SummaryService summaryService;

    public HomeController(MarketRepository marketRepository,
                          CommodityRepository commodityRepository,
                          PriceRecordRepository priceRecordRepository,
                          com.ampt.service.SummaryService summaryService) {
        this.marketRepository = marketRepository;
        this.commodityRepository = commodityRepository;
        this.priceRecordRepository = priceRecordRepository;
        this.summaryService = summaryService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("marketCount", marketRepository.count());
        model.addAttribute("commodityCount", commodityRepository.count());
        model.addAttribute("recordCount", priceRecordRepository.count());

        // B08, B09, B10: Populate daily commodity indicators
        var summaries = summaryService.getDailySummaries(null);
        model.addAttribute("summaries", summaries);
        if (!summaries.isEmpty()) {
            model.addAttribute("reportingDate", summaries.get(0).getReportDate());
        }
        return "dashboard";
    }

    @GetMapping("/dashboard/summary")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.List<com.ampt.dto.CommoditySummaryDto> getSummaryJson() {
        return summaryService.getDailySummaries(null);
    }
}
