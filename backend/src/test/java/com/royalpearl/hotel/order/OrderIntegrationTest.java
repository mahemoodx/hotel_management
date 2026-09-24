package com.royalpearl.hotel.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.royalpearl.hotel.AbstractIntegrationTest;
import com.royalpearl.hotel.order.repository.TableReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for food order creation (RPO- reference) and
 * payment-status protection.
 */
@DisplayName("Food Order Integration Tests")
class OrderIntegrationTest extends AbstractIntegrationTest {

    @Autowired MockMvc                   mockMvc;
    @Autowired ObjectMapper              mapper;
    @Autowired TableReservationRepository orderRepository;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Map<String, Object> buildOrderPayload(String name, String phone) {
        return Map.of(
                "name",        name,
                "phone",       phone,
                "orderType",   "dine_in",
                "items", List.of(
                        Map.of("name", "Hyderabadi Dum Biryani", "price", 350.00, "qty", 2),
                        Map.of("name", "Irani Chai",             "price",  60.00, "qty", 2)
                ),
                "total",          820.00,
                "paymentMethod", "cash"
        );
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Anonymous POST /orders returns 201 with RPO-…reference")
    void guestOrder_createsWithRpoReference() throws Exception {
        String phone = "81" + (10000000L + (long)(Math.random() * 10000000));

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(buildOrderPayload("Syed Basheer", phone))))
                .andExpect(status().isCreated())
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<String, Object> response = mapper.readValue(
                result.getResponse().getContentAsString(), Map.class);

        String orderId = (String) response.get("orderId");
        assertThat(orderId).isNotNull();
        assertThat(orderId).matches("RPO-\\d{7}");
    }

    @Test
    @DisplayName("Order reference is persisted in the database")
    void guestOrder_referencePersistedInDb() throws Exception {
        String phone = "82" + (10000000L + (long)(Math.random() * 10000000));

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(buildOrderPayload("Fatima", phone))))
                .andExpect(status().isCreated())
                .andReturn();

        @SuppressWarnings("unchecked")
        String orderId = (String) mapper.readValue(
                result.getResponse().getContentAsString(), Map.class).get("orderId");

        assertThat(orderRepository.findByOrderId(orderId)).isPresent();
    }

    @Test
    @DisplayName("Client-supplied paymentStatus=paid is ignored; stored as pending")
    void guestOrder_paymentStatusAlwaysPending() throws Exception {
        String phone = "83" + (10000000L + (long)(Math.random() * 10000000));
        Map<String, Object> payload = new java.util.HashMap<>(
                buildOrderPayload("Evil Client", phone));
        payload.put("paymentStatus", "paid");   // attempt escalation

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<String, Object> response = mapper.readValue(
                result.getResponse().getContentAsString(), Map.class);

        assertThat(response.get("paymentStatus")).isEqualTo("pending");
    }

    @Test
    @DisplayName("Order with empty cart returns 422")
    void order_emptyCart_returns422() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "name",  "Test",
                        "phone", "9000000001",
                        "items", List.of(),
                        "total", 0
                ))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Order lookup by reference+phone returns the order")
    void lookup_validRefAndPhone_returnsOrder() throws Exception {
        String phone = "9900000003";

        MvcResult created = mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(buildOrderPayload("Lookup User", phone))))
                .andExpect(status().isCreated())
                .andReturn();

        @SuppressWarnings("unchecked")
        String ref = (String) mapper.readValue(
                created.getResponse().getContentAsString(), Map.class).get("orderId");

        mockMvc.perform(get("/api/v1/orders/lookup")
                .param("reference", ref)
                .param("phone",     phone))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(ref));
    }
}
