package com.ecommerce.order.service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.domain.Product;
import com.ecommerce.order.dto.request.CreateOrderRequest;
import com.ecommerce.order.dto.request.OrderItemRequest;
import com.ecommerce.order.dto.response.OrderResponse;
import com.ecommerce.order.exception.InsufficientStockException;
import com.ecommerce.order.exception.InvalidOrderStatusException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.exception.ProductNotFoundException;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.debug("Buyurtmalar olib kelinmoqda");
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.debug("Buyurtma Id bo'yicha olinmoqda: {}", id);
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return OrderResponse.from(order);
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Xaridor uchun yangi buyurtma yaratish: {}", request.getCustomerEmail());
        validateNoDuplicateProducts(request.getItems());
        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(itemRequest.getProductId()));

            if (!product.getIsActive()) {
                throw new IllegalArgumentException(
                        "Product '" + product.getName() + " ushbu mahsulot mavjud emas");
            }
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        product.getName(), itemRequest.getQuantity(), product.getStock());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(itemTotal)
                    .build();

            order.getOrderItems().add(orderItem);
            totalAmount = totalAmount.add(itemTotal);
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);
            log.debug(" Manba yangilandi '{}': -{}", product.getName(), itemRequest.getQuantity());
        }

        order.setTotalAmount(totalAmount);
        Order saved = orderRepository.save(order);
        log.info(" Buyurtma muvaffaqiyatli amalga oshirildi: {}, total: {}", saved.getId(), totalAmount);
        return OrderResponse.from(saved);
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        log.info("Updating order {} status to {}", id, newStatus);
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException(order.getStatus(), newStatus);
        }

        if (newStatus == OrderStatus.PENDING) {
            throw new InvalidOrderStatusException(" Buyurtma kutish holatida");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        log.info("Order {} status updated to {}", id, newStatus);
        return OrderResponse.from(updated);
    }

    @Override
    public void cancelOrder(Long id) {
        log.info("Bekor qilingan buyurtma: {}", id);
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException(
                    " Buyurtmani bekor qila olmaysiz " +
                    "Current status: " + order.getStatus());
        }
        restoreStock(order);

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled and stock restored", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomerEmail(String email) {
        log.debug(" Xaridoe emaili uchun mahsulotlarni olib kelish: {}", email);
        return orderRepository.findByCustomerEmailWithItems(email).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    private void validateNoDuplicateProducts(List<OrderItemRequest> items) {
        Set<Long> seen = new HashSet<>();
        for (OrderItemRequest item : items) {
            if (!seen.add(item.getProductId())) {
                throw new IllegalArgumentException(
                        " duplikat product. Product ID " + item.getProductId() );
            }
        }
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
            log.debug(" do'kon qayta to'ldirildi '{}': +{}", product.getName(), item.getQuantity());
        }
    }
}
