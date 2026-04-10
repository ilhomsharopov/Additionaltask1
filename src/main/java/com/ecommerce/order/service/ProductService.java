package com.ecommerce.order.service;

import com.ecommerce.order.dto.request.CreateProductRequest;
import com.ecommerce.order.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<ProductResponse> getAllProducts(Pageable pageable);

    ProductResponse getProductById(Long id);

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(Long id, CreateProductRequest request);

    void deleteProduct(Long id);

    Page<ProductResponse> searchProducts(String name, String category, Pageable pageable);
}
