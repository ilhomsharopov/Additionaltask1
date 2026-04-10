package com.ecommerce.order.controller;

import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.request.CreateOrderRequest;
import com.ecommerce.order.dto.request.OrderItemRequest;
import com.ecommerce.order.dto.request.UpdateOrderStatusRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("OrderController Integration Tests")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/orders - Should return all orders")
    void getAllOrders_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Should return order by id")
    void getOrderById_WhenExists_ShouldReturnOrder() throws Exception {
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").isString())
                .andExpect(jsonPath("$.status").isString())
                .andExpect(jsonPath("$.orderItems").isArray());
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Should return 404 when not found")
    void getOrderById_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/orders/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Order Not Found"));
    }

    @Test
    @DisplayName("POST /api/orders - Should create order with valid data")
    void createOrder_WithValidData_ShouldReturn201() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Test Customer")
                .customerEmail("customer@test.com")
                .items(List.of(new OrderItemRequest(10L, 1))) // product id 10 = Logitech MX
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.customerEmail").value("customer@test.com"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").isNumber())
                .andExpect(jsonPath("$.orderItems").isArray())
                .andExpect(jsonPath("$.orderItems", hasSize(1)));
    }

    @Test
    @DisplayName("POST /api/orders - Should return 400 when email is invalid")
    void createOrder_WithInvalidEmail_ShouldReturn400() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Test Customer")
                .customerEmail("not-an-email")
                .items(List.of(new OrderItemRequest(1L, 1)))
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerEmail").exists());
    }

    @Test
    @DisplayName("POST /api/orders - Should return 400 when items list is empty")
    void createOrder_WithEmptyItems_ShouldReturn400() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Test Customer")
                .customerEmail("customer@test.com")
                .items(List.of())
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/orders - Should return 409 when stock is insufficient")
    void createOrder_WithInsufficientStock_ShouldReturn409() throws Exception {
        // Product id 12 has stock=0
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Test Customer")
                .customerEmail("customer@test.com")
                .items(List.of(new OrderItemRequest(12L, 5)))
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/status - Should update status of PENDING order")
    void updateOrderStatus_ForPendingOrder_ShouldSucceed() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);

        mockMvc.perform(put("/api/orders/3/status")  // order 3 = PENDING
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/status - Should return 400 for non-PENDING order")
    void updateOrderStatus_ForDeliveredOrder_ShouldReturn400() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);

        mockMvc.perform(put("/api/orders/1/status") // order 1 = DELIVERED
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - Should cancel PENDING order")
    void cancelOrder_WhenPending_ShouldReturn204() throws Exception {
        // Create a new order to cancel
        CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                .customerName("Cancel Test")
                .customerEmail("cancel@test.com")
                .items(List.of(new OrderItemRequest(11L, 1)))
                .build();

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long newOrderId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/orders/" + newOrderId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/orders/customer/{email} - Should return customer orders")
    void getOrdersByCustomerEmail_ShouldReturnOrders() throws Exception {
        mockMvc.perform(get("/api/orders/customer/alisher@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
