package com.ecommerce.order.service;

import com.ecommerce.order.domain.Product;
import com.ecommerce.order.dto.request.CreateProductRequest;
import com.ecommerce.order.dto.response.ProductResponse;
import com.ecommerce.order.exception.ProductNotFoundException;
import com.ecommerce.order.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.debug("hamma mahsulotlar olib kelinmoqda, page: {}", pageable.getPageNumber());
        return productRepository.findAll(pageable)
                .map(ProductResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.debug(" mahsulotni id bilan olib kelish: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductResponse.from(product);
    }

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("yangi mahsulot yaratish: {}", request.getName());
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        Product saved = productRepository.save(product);
        log.info("Mahsulot muvaffaqiyatli yaratildi: {}", saved.getId());
        return ProductResponse.from(saved);
    }

    @Override
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {
        log.info("mahsulot update qilindi: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }

        Product updated = productRepository.save(product);
        log.info("mahsulot update qilindi: {}", id);
        return ProductResponse.from(updated);
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String name, String category, Pageable pageable) {
        log.debug("Searching products - name: {}, category: {}", name, category);
        return productRepository.searchByNameAndCategory(name, category, pageable)
                .map(ProductResponse::from);
    }
}
