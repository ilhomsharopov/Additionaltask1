package com.ecommerce.order.controller;

import com.ecommerce.order.dto.request.CreateProductRequest;
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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ProductController Integration Tests")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/products - Should return paginated product list")
    void getAllProducts_ShouldReturnPagedList() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @DisplayName("GET /api/products/{id} - Should return product when exists")
    void getProductById_WhenExists_ShouldReturnProduct() throws Exception {
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.price").isNumber());
    }

    @Test
    @DisplayName("GET /api/products/{id} - Should return 404 when not found")
    void getProductById_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/products/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product Not Found"));
    }

    @Test
    @DisplayName("POST /api/products - Should create product with valid data")
    void createProduct_WithValidData_ShouldReturn201() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Test Product Integration")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .category("Test")
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Test Product Integration"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    @DisplayName("POST /api/products - Should return 400 when name is blank")
    void createProduct_WithBlankName_ShouldReturn400() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .category("Test")
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("POST /api/products - Should return 400 when price is negative")
    void createProduct_WithNegativePrice_ShouldReturn400() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Valid Name")
                .price(new BigDecimal("-10.00"))
                .stock(10)
                .category("Test")
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Should update product")
    void updateProduct_WhenExists_ShouldReturnUpdated() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Updated Product")
                .price(new BigDecimal("199.99"))
                .stock(20)
                .category("Updated Category")
                .isActive(true)
                .build();

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Should delete product")
    void deleteProduct_WhenExists_ShouldReturn204() throws Exception {
        // First create a product to delete
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Product To Delete")
                .price(new BigDecimal("10.00"))
                .stock(5)
                .category("Test")
                .isActive(true)
                .build();

        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long newId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/products/" + newId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/products/search - Should filter by name and category")
    void searchProducts_ShouldReturnFilteredResults() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("category", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
