package com.ecommerce.order.service;

import com.ecommerce.order.domain.*;
import com.ecommerce.order.dto.request.CreateOrderRequest;
import com.ecommerce.order.dto.request.OrderItemRequest;
import com.ecommerce.order.dto.response.OrderResponse;
import com.ecommerce.order.exception.*;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Product sampleProduct;
    private Order sampleOrder;
    private OrderItem sampleOrderItem;

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

        sampleOrder = Order.builder()
                .id(1L)
                .customerName("Alisher Karimov")
                .customerEmail("alisher@example.com")
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("1299.99"))
                .orderItems(new ArrayList<>())
                .build();

        sampleOrderItem = OrderItem.builder()
                .id(1L)
                .order(sampleOrder)
                .product(sampleProduct)
                .quantity(1)
                .unitPrice(new BigDecimal("1299.99"))
                .totalPrice(new BigDecimal("1299.99"))
                .build();

        sampleOrder.getOrderItems().add(sampleOrderItem);
    }

    @Test
    @DisplayName("Should return all orders")
    void getAllOrders_ShouldReturnAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(sampleOrder));

        List<OrderResponse> result = orderService.getAllOrders();

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getCustomerEmail()).isEqualTo("alisher@example.com");
    }

    @Test
    @DisplayName("Should return order by ID")
    void getOrderById_WhenExists_ShouldReturnOrder() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(sampleOrder));

        OrderResponse result = orderService.getOrderById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order not found")
    void getOrderById_WhenNotExists_ShouldThrowException() {
        when(orderRepository.findByIdWithItems(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should create order and deduct stock")
    void createOrder_WithValidData_ShouldCreateOrderAndDeductStock() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Alisher Karimov")
                .customerEmail("alisher@example.com")
                .items(List.of(new OrderItemRequest(1L, 2)))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        OrderResponse result = orderService.createOrder(request);

        assertThat(result).isNotNull();
        verify(productRepository, times(1)).save(any(Product.class)); // stock deducted
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when stock is not enough")
    void createOrder_WithInsufficientStock_ShouldThrowException() {
        sampleProduct.setStock(1); // only 1 in stock
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Test")
                .customerEmail("test@test.com")
                .items(List.of(new OrderItemRequest(1L, 5))) // requesting 5
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("iPhone 15 Pro");
    }

    @Test
    @DisplayName("Should throw exception when ordering inactive product")
    void createOrder_WithInactiveProduct_ShouldThrowException() {
        sampleProduct.setIsActive(false);
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Test")
                .customerEmail("test@test.com")
                .items(List.of(new OrderItemRequest(1L, 1)))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("Should throw exception for duplicate products in one order")
    void createOrder_WithDuplicateProducts_ShouldThrowException() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Test")
                .customerEmail("test@test.com")
                .items(List.of(
                        new OrderItemRequest(1L, 1),
                        new OrderItemRequest(1L, 2) // duplicate!
                ))
                .build();

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate product");
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException for non-existent product in order")
    void createOrder_WithNonExistentProduct_ShouldThrowException() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Test")
                .customerEmail("test@test.com")
                .items(List.of(new OrderItemRequest(999L, 1)))
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("Should update order status from PENDING to CONFIRMED")
    void updateOrderStatus_FromPendingToConfirmed_ShouldSucceed() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        OrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        assertThat(result).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-PENDING order")
    void updateOrderStatus_WhenNotPending_ShouldThrowException() {
        sampleOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED))
                .isInstanceOf(InvalidOrderStatusException.class);
    }

    @Test
    @DisplayName("Should cancel PENDING order and restore stock")
    void cancelOrder_WhenPending_ShouldCancelAndRestoreStock() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(sampleOrder));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        assertThatCode(() -> orderService.cancelOrder(1L)).doesNotThrowAnyException();

        verify(productRepository, times(1)).save(any(Product.class)); // stock restored
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling non-PENDING order")
    void cancelOrder_WhenNotPending_ShouldThrowException() {
        sampleOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("Should return orders by customer email")
    void getOrdersByCustomerEmail_ShouldReturnCustomerOrders() {
        when(orderRepository.findByCustomerEmailWithItems("alisher@example.com"))
                .thenReturn(List.of(sampleOrder));

        List<OrderResponse> result = orderService.getOrdersByCustomerEmail("alisher@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerEmail()).isEqualTo("alisher@example.com");
    }
}
