package com.ampt.service;

import com.ampt.dto.PriceRecordForm;
import com.ampt.entity.AppUser;
import com.ampt.entity.Commodity;
import com.ampt.entity.Market;
import com.ampt.entity.PriceRecord;
import com.ampt.entity.Role;
import com.ampt.repository.CommodityRepository;
import com.ampt.repository.MarketRepository;
import com.ampt.repository.PriceRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceRecordServiceTest {

    @Mock
    private PriceRecordRepository priceRecordRepository;

    @Mock
    private MarketRepository marketRepository;

    @Mock
    private CommodityRepository commodityRepository;

    @InjectMocks
    private PriceRecordService priceRecordService;

    private Market sampleMarket;
    private Commodity sampleCommodity;
    private AppUser sampleUser;

    @BeforeEach
    void setUp() {
        sampleMarket = new Market("Nagpur APMC", "Nagpur", "Maharashtra");
        sampleMarket.setId(1L);

        sampleCommodity = new Commodity("Tomato", "quintal");
        sampleCommodity.setId(1L);

        sampleUser = new AppUser("entry", "hashedPwd", Role.ENTRY_USER);
        sampleUser.setId(10L);
    }

    @Test
    @DisplayName("Validation passes when modal price is strictly between min and max price")
    void testValidateForm_Valid() {
        PriceRecordForm form = new PriceRecordForm();
        form.setMinPrice(new BigDecimal("1500.00"));
        form.setMaxPrice(new BigDecimal("2500.00"));
        form.setModalPrice(new BigDecimal("2000.00"));

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "priceForm");
        priceRecordService.validateForm(form, bindingResult);

        assertFalse(bindingResult.hasErrors());
    }

    @Test
    @DisplayName("Validation passes for uniform flat price where min == modal == max")
    void testValidateForm_UniformFlatPrice() {
        PriceRecordForm form = new PriceRecordForm();
        form.setMinPrice(new BigDecimal("2000.00"));
        form.setMaxPrice(new BigDecimal("2000.00"));
        form.setModalPrice(new BigDecimal("2000.00"));

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "priceForm");
        priceRecordService.validateForm(form, bindingResult);

        assertFalse(bindingResult.hasErrors());
    }

    @Test
    @DisplayName("Validation fails when min == max but modal price differs")
    void testValidateForm_UniformFlatPrice_ModalDiffers() {
        PriceRecordForm form = new PriceRecordForm();
        form.setMinPrice(new BigDecimal("2000.00"));
        form.setMaxPrice(new BigDecimal("2000.00"));
        form.setModalPrice(new BigDecimal("2050.00"));

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "priceForm");
        priceRecordService.validateForm(form, bindingResult);

        assertTrue(bindingResult.hasFieldErrors("modalPrice"));
    }

    @Test
    @DisplayName("Validation fails when min price is greater than max price")
    void testValidateForm_MinGreaterThanMax() {
        PriceRecordForm form = new PriceRecordForm();
        form.setMinPrice(new BigDecimal("2600.00"));
        form.setMaxPrice(new BigDecimal("2000.00"));
        form.setModalPrice(new BigDecimal("2200.00"));

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "priceForm");
        priceRecordService.validateForm(form, bindingResult);

        assertTrue(bindingResult.hasFieldErrors("minPrice"));
        assertEquals("Minimum price cannot be greater than maximum price.",
                bindingResult.getFieldError("minPrice").getDefaultMessage());
    }

    @Test
    @DisplayName("Validation fails when modal price is less than min price")
    void testValidateForm_ModalLessThanMin() {
        PriceRecordForm form = new PriceRecordForm();
        form.setMinPrice(new BigDecimal("1800.00"));
        form.setMaxPrice(new BigDecimal("2400.00"));
        form.setModalPrice(new BigDecimal("1500.00"));

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "priceForm");
        priceRecordService.validateForm(form, bindingResult);

        assertTrue(bindingResult.hasFieldErrors("modalPrice"));
        assertEquals("Modal price cannot be less than minimum price.",
                bindingResult.getFieldError("modalPrice").getDefaultMessage());
    }

    @Test
    @DisplayName("Validation fails when modal price is greater than max price")
    void testValidateForm_ModalGreaterThanMax() {
        PriceRecordForm form = new PriceRecordForm();
        form.setMinPrice(new BigDecimal("1800.00"));
        form.setMaxPrice(new BigDecimal("2400.00"));
        form.setModalPrice(new BigDecimal("2600.00"));

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "priceForm");
        priceRecordService.validateForm(form, bindingResult);

        assertTrue(bindingResult.hasFieldErrors("modalPrice"));
        assertEquals("Modal price cannot be greater than maximum price.",
                bindingResult.getFieldError("modalPrice").getDefaultMessage());
    }

    @Test
    @DisplayName("Create saves and returns a new PriceRecord with valid inputs")
    void testCreatePriceRecord_Success() {
        PriceRecordForm form = new PriceRecordForm();
        form.setMarketId(1L);
        form.setCommodityId(1L);
        form.setRecordDate(LocalDate.now());
        form.setMinPrice(new BigDecimal("1800.00"));
        form.setMaxPrice(new BigDecimal("2400.00"));
        form.setModalPrice(new BigDecimal("2100.00"));
        form.setArrivalQty(new BigDecimal("150.00"));

        when(marketRepository.findById(1L)).thenReturn(Optional.of(sampleMarket));
        when(commodityRepository.findById(1L)).thenReturn(Optional.of(sampleCommodity));
        when(priceRecordRepository.save(any(PriceRecord.class))).thenAnswer(invocation -> {
            PriceRecord pr = invocation.getArgument(0);
            pr.setId(99L);
            return pr;
        });

        PriceRecord created = priceRecordService.create(form, sampleUser);

        assertNotNull(created);
        assertEquals(99L, created.getId());
        assertEquals(sampleMarket, created.getMarket());
        assertEquals(sampleCommodity, created.getCommodity());
        assertEquals(new BigDecimal("2100.00"), created.getModalPrice());
        assertEquals(sampleUser, created.getCreatedBy());

        verify(priceRecordRepository, times(1)).save(any(PriceRecord.class));
    }

    @Test
    @DisplayName("Create throws IllegalArgumentException when market does not exist")
    void testCreatePriceRecord_InvalidMarket() {
        PriceRecordForm form = new PriceRecordForm();
        form.setMarketId(999L);
        form.setCommodityId(1L);

        when(marketRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> priceRecordService.create(form, sampleUser));
        verify(priceRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update modifies fields of an existing PriceRecord")
    void testUpdatePriceRecord_Success() {
        PriceRecord existing = new PriceRecord();
        existing.setId(42L);
        existing.setMarket(sampleMarket);
        existing.setCommodity(sampleCommodity);
        existing.setMinPrice(new BigDecimal("1000.00"));
        existing.setMaxPrice(new BigDecimal("1500.00"));
        existing.setModalPrice(new BigDecimal("1200.00"));

        PriceRecordForm form = new PriceRecordForm();
        form.setMarketId(1L);
        form.setCommodityId(1L);
        form.setRecordDate(LocalDate.now());
        form.setMinPrice(new BigDecimal("1200.00"));
        form.setMaxPrice(new BigDecimal("1800.00"));
        form.setModalPrice(new BigDecimal("1500.00"));
        form.setArrivalQty(new BigDecimal("200.00"));

        when(priceRecordRepository.findById(42L)).thenReturn(Optional.of(existing));
        when(marketRepository.findById(1L)).thenReturn(Optional.of(sampleMarket));
        when(commodityRepository.findById(1L)).thenReturn(Optional.of(sampleCommodity));
        when(priceRecordRepository.save(existing)).thenReturn(existing);

        PriceRecord updated = priceRecordService.update(42L, form, sampleUser);

        assertEquals(new BigDecimal("1500.00"), updated.getModalPrice());
        assertEquals(new BigDecimal("1800.00"), updated.getMaxPrice());
        verify(priceRecordRepository).save(existing);
    }
}
