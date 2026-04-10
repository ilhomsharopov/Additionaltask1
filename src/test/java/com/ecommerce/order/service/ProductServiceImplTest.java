package com.ecommerce.order.service;

import com.ecommerce.order.domain.Product;
import com.ecommerce.order.dto.request.CreateProductRequest;
import com.ecommerce.order.dto.response.ProductResponse;
import com.ecommerce.order.exception.ProductNotFoundException;
import com.ecommerce.order.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleProduct;
    private CreateProductRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("iPhone 15 Pro")
                .price(new BigDecimal("1299.99"))
                .stock(50)
                .category("Electronics")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = CreateProductRequest.builder()
                .name("iPhone 15 Pro")
                .price(new BigDecimal("1299.99"))
                .stock(50)
                .category("Electronics")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should return all products with pagination")
    void getAllProducts_ShouldReturnPagedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<ProductResponse> result = productService.getAllProducts(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("iPhone 15 Pro");
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should return product when found by ID")
    void getProductById_WhenExists_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductResponse result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("iPhone 15 Pro");
        assertThat(result.getPrice()).isEqualByComparingTo("1299.99");
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product not found")
    void getProductById_WhenNotExists_ShouldThrowException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should create product and return response")
    void createProduct_WithValidRequest_ShouldCreateAndReturn() {
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse result = productService.createProduct(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("iPhone 15 Pro");
        assertThat(result.getPrice()).isEqualByComparingTo("1299.99");
        assertThat(result.getStock()).isEqualTo(50);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product and return updated response")
    void updateProduct_WhenExists_ShouldUpdateAndReturn() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        CreateProductRequest updateRequest = CreateProductRequest.builder()
                .name("iPhone 15 Pro Max")
                .price(new BigDecimal("1499.99"))
                .stock(40)
                .category("Electronics")
                .isActive(true)
                .build();

        ProductResponse result = productService.updateProduct(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent product")
    void updateProduct_WhenNotExists_ShouldThrowException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(999L, createRequest))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete product when exists")
    void deleteProduct_WhenExists_ShouldDelete() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        assertThatCode(() -> productService.deleteProduct(1L)).doesNotThrowAnyException();
        verify(productRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void deleteProduct_WhenNotExists_ShouldThrowException() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(ProductNotFoundException.class);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should search products by name and category")
    void searchProducts_ShouldReturnFilteredProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.searchByNameAndCategory("iPhone", "Electronics", pageable))
                .thenReturn(page);

        Page<ProductResponse> result = productService.searchProducts("iPhone", "Electronics", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
