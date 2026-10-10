package com.ampt.config;

import com.ampt.entity.*;
import com.ampt.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds realistic data for Task 5:
 * - 2 Users (entry / viewer) with BCrypt hashed passwords
 * - 3 Markets (Nagpur APMC, Pune APMC, Indore Mandi)
 * - 3 Commodities (Tomato, Onion, Wheat)
 * - 14 Days of realistic price records across all markets and commodities (126 records)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AppUserRepository userRepository;
    private final MarketRepository marketRepository;
    private final CommodityRepository commodityRepository;
    private final PriceRecordRepository priceRecordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AppUserRepository userRepository,
                           MarketRepository marketRepository,
                           CommodityRepository commodityRepository,
                           PriceRecordRepository priceRecordRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.marketRepository = marketRepository;
        this.commodityRepository = commodityRepository;
        this.priceRecordRepository = priceRecordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        List<Market> markets = seedMarkets();
        List<Commodity> commodities = seedCommodities();
        seedPriceRecords(markets, commodities);
    }

    private void seedUsers() {
        if (userRepository.findByUsername("entry").isEmpty()) {
            AppUser entryUser = new AppUser("entry", passwordEncoder.encode("entry123"), Role.ENTRY_USER);
            userRepository.save(entryUser);
            log.info("Seeded user 'entry' with role ENTRY_USER");
        }
        if (userRepository.findByUsername("viewer").isEmpty()) {
            AppUser viewerUser = new AppUser("viewer", passwordEncoder.encode("viewer123"), Role.VIEWER);
            userRepository.save(viewerUser);
            log.info("Seeded user 'viewer' with role VIEWER");
        }
    }

    private List<Market> seedMarkets() {
        if (marketRepository.count() == 0) {
            marketRepository.save(new Market("Nagpur APMC", "Nagpur", "Maharashtra"));
            marketRepository.save(new Market("Pune APMC", "Pune", "Maharashtra"));
            marketRepository.save(new Market("Indore Mandi", "Indore", "Madhya Pradesh"));
        }
        return marketRepository.findAll();
    }

    private List<Commodity> seedCommodities() {
        if (commodityRepository.count() == 0) {
            commodityRepository.save(new Commodity("Tomato", "quintal"));
            commodityRepository.save(new Commodity("Onion", "quintal"));
            commodityRepository.save(new Commodity("Wheat", "quintal"));
        }
        return commodityRepository.findAll();
    }

    private void seedPriceRecords(List<Market> markets, List<Commodity> commodities) {
        if (priceRecordRepository.count() > 0) {
            return;
        }

        AppUser entryUser = userRepository.findByUsername("entry").orElse(null);
        LocalDate endDate = LocalDate.of(2026, 10, 10);
        List<PriceRecord> records = new ArrayList<>();

        // Base prices per commodity (min, modal, max)
        // Tomato: ~1800-2400
        // Onion: ~1400-2000
        // Wheat: ~2300-2700
        for (int dayOffset = 13; dayOffset >= 0; dayOffset--) {
            LocalDate date = endDate.minusDays(dayOffset);

            for (Market market : markets) {
                for (Commodity commodity : commodities) {
                    BigDecimal basePrice;
                    BigDecimal spread;
                    BigDecimal arrivalBase;

                    switch (commodity.getName().toLowerCase()) {
                        case "tomato" -> {
                            basePrice = BigDecimal.valueOf(2000 + (dayOffset * 15) + (market.getId() * 30));
                            spread = BigDecimal.valueOf(250);
                            arrivalBase = BigDecimal.valueOf(180 + (dayOffset * 5));
                        }
                        case "onion" -> {
                            basePrice = BigDecimal.valueOf(1600 + (dayOffset * 10) + (market.getId() * 20));
                            spread = BigDecimal.valueOf(200);
                            arrivalBase = BigDecimal.valueOf(320 + (dayOffset * 8));
                        }
                        default -> { // Wheat
                            basePrice = BigDecimal.valueOf(2450 + (dayOffset * 8) + (market.getId() * 15));
                            spread = BigDecimal.valueOf(180);
                            arrivalBase = BigDecimal.valueOf(450 + (dayOffset * 10));
                        }
                    }

                    BigDecimal min = basePrice.subtract(spread);
                    BigDecimal max = basePrice.add(spread);
                    BigDecimal modal = basePrice.add(BigDecimal.valueOf((dayOffset % 3 - 1) * 20));

                    PriceRecord record = new PriceRecord();
                    record.setMarket(market);
                    record.setCommodity(commodity);
                    record.setRecordDate(date);
                    record.setMinPrice(min);
                    record.setMaxPrice(max);
                    record.setModalPrice(modal);
                    record.setArrivalQty(arrivalBase);
                    record.setCreatedBy(entryUser);

                    records.add(record);
                }
            }
        }

        priceRecordRepository.saveAll(records);
        log.info("Successfully seeded {} realistic price records across 14 days", records.size());
    }
}
