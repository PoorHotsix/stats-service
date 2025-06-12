package com.inkcloud.stats_service.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesStat {
    
    private String dateType; //daily, weekly, monthly

    @DynamoDbPartitionKey
    public String getDateType() {
        return dateType;
    }

    private String dateKey; //yyyy-MM-dd, yyyy-ww, yyyy-MM

    @DynamoDbSortKey
    public String getDateKey() {
        return dateKey;
    }

    private Long totalSales; //총 매출액

    private Long orderCount; //총 주문 수

    private Long itemCount; //총 주문 아이템 수
}
