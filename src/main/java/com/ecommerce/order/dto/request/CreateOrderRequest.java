package com.ecommerce.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotBlank(message = "xaridorning ismi bo'lishi kerak")
    private String customerName;

    @NotBlank(message = "xaridorningemaili bo'lishi kerak")
    @Email(message = "noto'g'ri email formati")
    private String customerEmail;

    @NotEmpty(message = "buyurtmada kamida 1 ta mahsulot bo'lishi kerak")
    @Valid
    private List<OrderItemRequest> items;
}
