package com.ampt.controller;

import com.ampt.entity.*;
import com.ampt.service.PriceRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PriceRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriceRecordService priceRecordService;

    @MockBean
    private com.ampt.service.SummaryService summaryService;

    private AppUser entryUser;
    private AppUser viewerUser;
    private Market sampleMarket;
    private Commodity sampleCommodity;
    private PriceRecord sampleRecord;

    @BeforeEach
    void setUp() {
        entryUser = new AppUser("entry", "hashed", Role.ENTRY_USER);
        entryUser.setId(1L);

        viewerUser = new AppUser("viewer", "hashed", Role.VIEWER);
        viewerUser.setId(2L);

        sampleMarket = new Market("Nagpur APMC", "Nagpur", "Maharashtra");
        sampleMarket.setId(1L);

        sampleCommodity = new Commodity("Tomato", "quintal");
        sampleCommodity.setId(1L);

        sampleRecord = new PriceRecord();
        sampleRecord.setId(101L);
        sampleRecord.setMarket(sampleMarket);
        sampleRecord.setCommodity(sampleCommodity);
        sampleRecord.setRecordDate(LocalDate.now());
        sampleRecord.setMinPrice(new BigDecimal("1800.00"));
        sampleRecord.setMaxPrice(new BigDecimal("2400.00"));
        sampleRecord.setModalPrice(new BigDecimal("2100.00"));
        sampleRecord.setArrivalQty(new BigDecimal("120.00"));
        sampleRecord.setCreatedBy(entryUser);

        when(priceRecordService.findAllMarkets()).thenReturn(List.of(sampleMarket));
        when(priceRecordService.findAllCommodities()).thenReturn(List.of(sampleCommodity));
    }

    @Test
    @DisplayName("B06/B07: GET /prices returns 200 and loads search page with records")
    void testSearchPrices_PublicAccess() throws Exception {
        when(priceRecordService.search(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleRecord)));

        mockMvc.perform(get("/prices"))
                .andExpect(status().isOk())
                .andExpect(view().name("prices/search"))
                .andExpect(model().attributeExists("records"))
                .andExpect(model().attributeExists("markets"))
                .andExpect(model().attributeExists("commodities"));
    }

    @Test
    @DisplayName("B05: GET /prices/new without session redirects to /login")
    void testNewPrice_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/prices/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login?redirect=")));
    }

    @Test
    @DisplayName("B05: GET /prices/new with VIEWER role returns 403 Forbidden")
    void testNewPrice_ViewerRole_Forbidden() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_KEY, viewerUser);

        mockMvc.perform(get("/prices/new").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("B03/B05: GET /prices/new with ENTRY_USER role returns 200 OK")
    void testNewPrice_EntryUser_Success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_KEY, entryUser);

        mockMvc.perform(get("/prices/new").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("prices/form"))
                .andExpect(model().attributeExists("priceForm"))
                .andExpect(model().attribute("isEdit", false));
    }

    @Test
    @DisplayName("B03: POST /prices with valid data creates record and redirects")
    void testCreatePrice_Valid_Success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_KEY, entryUser);

        when(priceRecordService.create(any(), any())).thenReturn(sampleRecord);

        mockMvc.perform(post("/prices")
                        .session(session)
                        .param("marketId", "1")
                        .param("commodityId", "1")
                        .param("recordDate", "2026-10-10")
                        .param("minPrice", "1800.00")
                        .param("maxPrice", "2400.00")
                        .param("modalPrice", "2100.00")
                        .param("arrivalQty", "150.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prices"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(priceRecordService, times(1)).create(any(), any());
    }

    @Test
    @DisplayName("B03: POST /prices with validation failure redisplays form")
    void testCreatePrice_ValidationFailure_ShowsForm() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_KEY, entryUser);

        // Intentionally trigger validation by setting minPrice > maxPrice
        doAnswer(invocation -> {
            org.springframework.validation.BindingResult result = invocation.getArgument(1);
            result.rejectValue("modalPrice", "error.modalPrice", "Modal price error");
            return null;
        }).when(priceRecordService).validateForm(any(), any());

        mockMvc.perform(post("/prices")
                        .session(session)
                        .param("marketId", "1")
                        .param("commodityId", "1")
                        .param("recordDate", "2026-10-10")
                        .param("minPrice", "1800.00")
                        .param("maxPrice", "2400.00")
                        .param("modalPrice", "3000.00") // Out of range
                        .param("arrivalQty", "150.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("prices/form"))
                .andExpect(model().attributeHasFieldErrors("priceForm", "modalPrice"));

        verify(priceRecordService, never()).create(any(), any());
    }

    @Test
    @DisplayName("B04: GET /prices/{id}/edit loads existing record into form")
    void testEditPrice_EntryUser_Success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_KEY, entryUser);

        when(priceRecordService.getById(101L)).thenReturn(Optional.of(sampleRecord));

        mockMvc.perform(get("/prices/101/edit").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("prices/form"))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attributeExists("priceForm"));
    }

    @Test
    @DisplayName("B12: GET /prices/history renders history view with trend data")
    void testViewHistory_PublicAccess() throws Exception {
        when(summaryService.getMarketCommodityHistory(any(), any())).thenReturn(List.of(sampleRecord));

        mockMvc.perform(get("/prices/history").param("marketId", "1").param("commodityId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("prices/history"))
                .andExpect(model().attributeExists("historyRecords"))
                .andExpect(model().attributeExists("allTimeMin"))
                .andExpect(model().attributeExists("allTimeMax"))
                .andExpect(model().attributeExists("avgModal"));
    }
}
