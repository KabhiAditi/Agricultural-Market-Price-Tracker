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

    public HomeController(MarketRepository marketRepository,
                           CommodityRepository commodityRepository,
                           PriceRecordRepository priceRecordRepository) {
        this.marketRepository = marketRepository;
        this.commodityRepository = commodityRepository;
        this.priceRecordRepository = priceRecordRepository;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("marketCount", marketRepository.count());
        model.addAttribute("commodityCount", commodityRepository.count());
        model.addAttribute("recordCount", priceRecordRepository.count());
        return "dashboard";
    }
}
