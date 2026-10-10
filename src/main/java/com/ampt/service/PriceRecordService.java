package com.ampt.service;

import com.ampt.dto.PriceRecordForm;
import com.ampt.entity.AppUser;
import com.ampt.entity.Commodity;
import com.ampt.entity.Market;
import com.ampt.entity.PriceRecord;
import com.ampt.repository.CommodityRepository;
import com.ampt.repository.MarketRepository;
import com.ampt.repository.PriceRecordRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PriceRecordService {

    private final PriceRecordRepository priceRecordRepository;
    private final MarketRepository marketRepository;
    private final CommodityRepository commodityRepository;

    public PriceRecordService(PriceRecordRepository priceRecordRepository,
                              MarketRepository marketRepository,
                              CommodityRepository commodityRepository) {
        this.priceRecordRepository = priceRecordRepository;
        this.marketRepository = marketRepository;
        this.commodityRepository = commodityRepository;
    }

    /**
     * Validates domain constraints: modalPrice between minPrice and maxPrice, minPrice <= maxPrice.
     */
    public void validateForm(PriceRecordForm form, BindingResult bindingResult) {
        if (form.getMinPrice() != null && form.getMaxPrice() != null) {
            if (form.getMinPrice().compareTo(form.getMaxPrice()) > 0) {
                bindingResult.rejectValue("minPrice", "error.minPrice",
                        "Minimum price cannot be greater than maximum price.");
            }
        }

        if (form.getModalPrice() != null && form.getMinPrice() != null) {
            if (form.getModalPrice().compareTo(form.getMinPrice()) < 0) {
                bindingResult.rejectValue("modalPrice", "error.modalPrice",
                        "Modal price cannot be less than minimum price.");
            }
        }

        if (form.getModalPrice() != null && form.getMaxPrice() != null) {
            if (form.getModalPrice().compareTo(form.getMaxPrice()) > 0) {
                bindingResult.rejectValue("modalPrice", "error.modalPrice",
                        "Modal price cannot be greater than maximum price.");
            }
        }

        // Self-Review Fix: If min equals max, modal must equal both
        if (form.getMinPrice() != null && form.getMaxPrice() != null && form.getModalPrice() != null) {
            if (form.getMinPrice().compareTo(form.getMaxPrice()) == 0) {
                if (form.getModalPrice().compareTo(form.getMinPrice()) != 0) {
                    bindingResult.rejectValue("modalPrice", "error.modalPrice",
                            "When minimum price equals maximum price, modal price must equal both.");
                }
            }
        }
    }

    public PriceRecord create(PriceRecordForm form, AppUser user) {
        Market market = marketRepository.findById(form.getMarketId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid market ID: " + form.getMarketId()));
        Commodity commodity = commodityRepository.findById(form.getCommodityId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid commodity ID: " + form.getCommodityId()));

        PriceRecord record = new PriceRecord();
        record.setMarket(market);
        record.setCommodity(commodity);
        record.setRecordDate(form.getRecordDate());
        record.setMinPrice(form.getMinPrice());
        record.setMaxPrice(form.getMaxPrice());
        record.setModalPrice(form.getModalPrice());
        record.setArrivalQty(form.getArrivalQty());
        record.setCreatedBy(user);

        return priceRecordRepository.save(record);
    }

    public PriceRecord update(Long id, PriceRecordForm form, AppUser user) {
        PriceRecord record = priceRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Price record not found with ID: " + id));

        Market market = marketRepository.findById(form.getMarketId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid market ID: " + form.getMarketId()));
        Commodity commodity = commodityRepository.findById(form.getCommodityId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid commodity ID: " + form.getCommodityId()));

        record.setMarket(market);
        record.setCommodity(commodity);
        record.setRecordDate(form.getRecordDate());
        record.setMinPrice(form.getMinPrice());
        record.setMaxPrice(form.getMaxPrice());
        record.setModalPrice(form.getModalPrice());
        record.setArrivalQty(form.getArrivalQty());

        return priceRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public Optional<PriceRecord> getById(Long id) {
        return priceRecordRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<PriceRecord> search(Long marketId, Long commodityId, LocalDate from, LocalDate to, Pageable pageable) {
        Specification<PriceRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (marketId != null) {
                predicates.add(cb.equal(root.get("market").get("id"), marketId));
            }
            if (commodityId != null) {
                predicates.add(cb.equal(root.get("commodity").get("id"), commodityId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("recordDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("recordDate"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return priceRecordRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<Market> findAllMarkets() {
        return marketRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Commodity> findAllCommodities() {
        return commodityRepository.findAll();
    }
}
