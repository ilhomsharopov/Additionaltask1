package com.ecommerce.order.repository;

import com.ecommerce.order.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductRepository Integration Tests")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Should find product by id when it exists")
    void findById_WhenExists_ShouldReturnProduct() {
        Product product = createAndSaveProduct("Test Repo Product", "Electronics");

        var found = productRepository.findById(product.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Repo Product");
    }

    @Test
    @DisplayName("Should return empty when product not found by id")
    void findById_WhenNotExists_ShouldReturnEmpty() {
        var found = productRepository.findById(99999L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should search products by name containing keyword")
    void searchByNameAndCategory_WithName_ShouldFilterByName() {
        createAndSaveProduct("Samsung Galaxy S24", "Electronics");
        createAndSaveProduct("Samsung TV 55", "Electronics");
        createAndSaveProduct("Apple iPhone 15", "Electronics");

        Page<Product> result = productRepository.searchByNameAndCategory(
                "Samsung", null, PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).allMatch(p -> p.getName().contains("Samsung"));
    }

    @Test
    @DisplayName("Should search products by category")
    void searchByNameAndCategory_WithCategory_ShouldFilterByCategory() {
        createAndSaveProduct("Nike Shoes", "Shoes");
        createAndSaveProduct("Adidas Shoes", "Shoes");
        createAndSaveProduct("Apple MacBook", "Computers");

        Page<Product> result = productRepository.searchByNameAndCategory(
                null, "Shoes", PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).allMatch(p -> p.getCategory().equalsIgnoreCase("Shoes"));
    }

    @Test
    @DisplayName("Should search products by both name and category")
    void searchByNameAndCategory_WithBoth_ShouldFilter() {
        createAndSaveProduct("Sony WH Headphones", "Audio");
        createAndSaveProduct("Sony TV 4K", "Electronics");

        Page<Product> result = productRepository.searchByNameAndCategory(
                "Sony", "Audio", PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
        result.getContent().forEach(p -> {
            assertThat(p.getName()).containsIgnoringCase("Sony");
            assertThat(p.getCategory()).equalsIgnoringCase("Audio");
        });
    }

    @Test
    @DisplayName("Should save and retrieve product with all fields")
    void save_ShouldPersistAllFields() {
        Product product = Product.builder()
                .name("Full Fields Product")
                .price(new BigDecimal("500.00"))
                .stock(100)
                .category("Test Category")
                .isActive(true)
                .build();

        Product saved = productRepository.save(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Full Fields Product");
        assertThat(saved.getPrice()).isEqualByComparingTo("500.00");
        assertThat(saved.getStock()).isEqualTo(100);
        assertThat(saved.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should delete product by id")
    void delete_ShouldRemoveProduct() {
        Product product = createAndSaveProduct("Product To Delete", "Test");
        Long id = product.getId();

        productRepository.deleteById(id);

        assertThat(productRepository.findById(id)).isEmpty();
    }

    private Product createAndSaveProduct(String name, String category) {
        Product product = Product.builder()
                .name(name)
                .price(new BigDecimal("100.00"))
                .stock(10)
                .category(category)
                .isActive(true)
                .build();
        return productRepository.save(product);
    }
}
