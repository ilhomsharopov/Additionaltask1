package com.ecommerce.order.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank(message = "mahsulot nomi bo'lishi shart")
    @Size(min = 2, max = 255, message = "mahsulot nomi 2 ta belgidan 255 tagacha belgidan iborat bo'lishi mumkin")
    private String name;

    @NotNull(message = "Narx bo'lishi kerak")
    @DecimalMin(value = "0.01", message = "Narx 0 dan katta bo'lishi kerak")
    @Digits(integer = 8, fraction = 2, message = "Narx noto'g'ri farmatdakiritildi")
    private BigDecimal price;

    @NotNull(message = "Stock bo'lishi kerak")
    @Min(value = 0, message = "Stock manfiy bo'la olmaydi")
    private Integer stock;

    @NotBlank(message = "Kategoriya bo'lishi kerak")
    @Size(min = 2, max = 100, message = "2 dan 100 gacha bo'lgan belgilardan tashkil topgan bo'lishi kerak")
    private String category;

    private Boolean isActive = true;
}
