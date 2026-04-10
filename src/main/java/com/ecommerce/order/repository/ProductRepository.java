package com.ecommerce.order.repository;

import com.ecommerce.order.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR LOWER(p.category) = LOWER(:category))")
    Page<Product> searchByNameAndCategory(
            @Param("name") String name,
            @Param("category") String category,
            Pageable pageable);

    Page<Product> findAll(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
}
