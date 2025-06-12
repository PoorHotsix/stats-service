package com.inkcloud.stats_service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventDto {

    private String orderId;
    private int totalQuantity;
    private int totalSales;
    private LocalDateTime createdAt;
    
}
