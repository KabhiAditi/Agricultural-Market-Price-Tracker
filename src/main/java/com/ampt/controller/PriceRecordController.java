package com.ampt.controller;

import com.ampt.dto.PriceRecordForm;
import com.ampt.entity.AppUser;
import com.ampt.entity.PriceRecord;
import com.ampt.service.PriceRecordService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/prices")
public class PriceRecordController {

    private final PriceRecordService priceRecordService;
    private final com.ampt.service.SummaryService summaryService;

    public PriceRecordController(PriceRecordService priceRecordService,
                                com.ampt.service.SummaryService summaryService) {
        this.priceRecordService = priceRecordService;
        this.summaryService = summaryService;
    }

    /**
     * B12: Single-record / full-history detail view for market + commodity.
     */
    @GetMapping("/history")
    public String viewHistory(
            @RequestParam(required = false) Long marketId,
            @RequestParam(required = false) Long commodityId,
            Model model) {

        var markets = priceRecordService.findAllMarkets();
        var commodities = priceRecordService.findAllCommodities();

        Long targetMarketId = (marketId != null) ? marketId : (markets.isEmpty() ? null : markets.get(0).getId());
        Long targetCommodityId = (commodityId != null) ? commodityId : (commodities.isEmpty() ? null : commodities.get(0).getId());

        var historyList = summaryService.getMarketCommodityHistory(targetMarketId, targetCommodityId);

        model.addAttribute("markets", markets);
        model.addAttribute("commodities", commodities);
        model.addAttribute("selectedMarketId", targetMarketId);
        model.addAttribute("selectedCommodityId", targetCommodityId);
        model.addAttribute("historyRecords", historyList);

        if (!historyList.isEmpty()) {
            java.math.BigDecimal allTimeMin = historyList.stream()
                    .map(com.ampt.entity.PriceRecord::getMinPrice)
                    .min(java.util.Comparator.naturalOrder())
                    .orElse(java.math.BigDecimal.ZERO);
            java.math.BigDecimal allTimeMax = historyList.stream()
                    .map(com.ampt.entity.PriceRecord::getMaxPrice)
                    .max(java.util.Comparator.naturalOrder())
                    .orElse(java.math.BigDecimal.ZERO);
            java.math.BigDecimal avgModal = historyList.stream()
                    .map(com.ampt.entity.PriceRecord::getModalPrice)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                    .divide(java.math.BigDecimal.valueOf(historyList.size()), 2, java.math.RoundingMode.HALF_UP);

            model.addAttribute("allTimeMin", allTimeMin);
            model.addAttribute("allTimeMax", allTimeMax);
            model.addAttribute("avgModal", avgModal);
        }

        return "prices/history";
    }

    /**
     * B06 + B07: Search prices with filters, pagination (10/page), and sorting.
     */
    @GetMapping
    public String searchPrices(
            @RequestParam(required = false) Long marketId,
            @RequestParam(required = false) Long commodityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recordDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        // Self-Review Fix: Handle inverted date filter gracefully
        if (from != null && to != null && from.isAfter(to)) {
            LocalDate temp = from;
            from = to;
            to = temp;
            model.addAttribute("infoMessage", "Notice: Search date range was inverted and has been automatically ordered (" + from + " to " + to + ").");
        }

        Sort sort = "asc".equalsIgnoreCase(sortDir) ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(Math.max(0, page), size, sort);
        Page<PriceRecord> records = priceRecordService.search(marketId, commodityId, from, to, pageable);

        model.addAttribute("records", records);
        model.addAttribute("markets", priceRecordService.findAllMarkets());
        model.addAttribute("commodities", priceRecordService.findAllCommodities());

        // Retain search criteria in view
        model.addAttribute("selectedMarketId", marketId);
        model.addAttribute("selectedCommodityId", commodityId);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", records.getTotalPages());
        model.addAttribute("totalElements", records.getTotalElements());

        return "prices/search";
    }

    /**
     * B03: Show new price entry form.
     */
    @GetMapping("/new")
    public String newPriceForm(Model model) {
        PriceRecordForm form = new PriceRecordForm();
        form.setRecordDate(LocalDate.now());

        model.addAttribute("priceForm", form);
        model.addAttribute("markets", priceRecordService.findAllMarkets());
        model.addAttribute("commodities", priceRecordService.findAllCommodities());
        model.addAttribute("isEdit", false);
        return "prices/form";
    }

    /**
     * B03: Handle creation of new price record.
     */
    @PostMapping
    public String createPrice(
            @Valid @ModelAttribute("priceForm") PriceRecordForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        priceRecordService.validateForm(form, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("markets", priceRecordService.findAllMarkets());
            model.addAttribute("commodities", priceRecordService.findAllCommodities());
            model.addAttribute("isEdit", false);
            return "prices/form";
        }

        AppUser user = (AppUser) session.getAttribute(AuthController.SESSION_USER_KEY);
        PriceRecord created = priceRecordService.create(form, user);

        redirectAttributes.addFlashAttribute("successMessage",
                "Price record #" + created.getId() + " for " + created.getCommodity().getName() +
                        " at " + created.getMarket().getName() + " saved successfully.");
        return "redirect:/prices";
    }

    /**
     * B04: Show edit form for existing price record.
     */
    @GetMapping("/{id}/edit")
    public String editPriceForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        PriceRecord record = priceRecordService.getById(id).orElse(null);
        if (record == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Price record #" + id + " not found.");
            return "redirect:/prices";
        }

        PriceRecordForm form = new PriceRecordForm();
        form.setId(record.getId());
        form.setMarketId(record.getMarket().getId());
        form.setCommodityId(record.getCommodity().getId());
        form.setRecordDate(record.getRecordDate());
        form.setMinPrice(record.getMinPrice());
        form.setMaxPrice(record.getMaxPrice());
        form.setModalPrice(record.getModalPrice());
        form.setArrivalQty(record.getArrivalQty());

        model.addAttribute("priceForm", form);
        model.addAttribute("markets", priceRecordService.findAllMarkets());
        model.addAttribute("commodities", priceRecordService.findAllCommodities());
        model.addAttribute("isEdit", true);
        return "prices/form";
    }

    /**
     * B04: Handle update of existing price record.
     */
    @PostMapping("/{id}")
    public String updatePrice(
            @PathVariable Long id,
            @Valid @ModelAttribute("priceForm") PriceRecordForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        priceRecordService.validateForm(form, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("markets", priceRecordService.findAllMarkets());
            model.addAttribute("commodities", priceRecordService.findAllCommodities());
            model.addAttribute("isEdit", true);
            return "prices/form";
        }

        AppUser user = (AppUser) session.getAttribute(AuthController.SESSION_USER_KEY);
        PriceRecord updated = priceRecordService.update(id, form, user);

        redirectAttributes.addFlashAttribute("successMessage",
                "Price record #" + updated.getId() + " for " + updated.getCommodity().getName() +
                        " updated successfully.");
        return "redirect:/prices";
    }
}
