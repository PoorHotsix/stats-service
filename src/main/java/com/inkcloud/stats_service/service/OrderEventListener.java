package com.inkcloud.stats_service.service;

import com.inkcloud.stats_service.dto.OrderEventDto;
import com.inkcloud.stats_service.domain.SalesStat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventListener {

    private final DynamoDbEnhancedClient enhancedClient;
    private final ObjectMapper objectMapper;

    //주문생성시 테이블에 업데이트
    @KafkaListener(topics = "order-complete-stat", groupId = "stats-service")
    public void handleOrderCreatedEvent(String message) {
        try {
            // 1. JSON -> DTO 변환
            OrderEventDto event = objectMapper.readValue(message, OrderEventDto.class);

            // 2. 날짜별 집계키 생성
            LocalDateTime createdAt = event.getCreatedAt();
            // 일간: daily, yyyy-MM-dd
            upsertStat("daily", createdAt.format(DateTimeFormatter.ISO_DATE), event);

            // 주간: weekly, yyyy-ww (ISO 기준)
            int week = createdAt.toLocalDate().get(WeekFields.ISO.weekOfWeekBasedYear());
            String weekKey = String.format("%d-%02d", createdAt.getYear(), week);
            upsertStat("weekly", weekKey, event);

            // 월간: monthly, yyyy-MM
            upsertStat("monthly", createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM")), event);
            log.info("order-complete-stat event :{}", event);
        } catch (Exception e) {
            log.error("OrderEventListener 처리 중 오류", e);
        }
    }

    private void upsertStat(String dateType, String dateKey, OrderEventDto event) {
        DynamoDbTable<SalesStat> table = enhancedClient.table("SalesStat", TableSchema.fromBean(SalesStat.class));
        SalesStat stat = table.getItem(Key.builder().partitionValue(dateType).sortValue(dateKey).build());
        if (stat == null) {
            stat = new SalesStat(dateType, dateKey, (long) event.getTotalSales(),1L, (long) event.getTotalQuantity());
        } else {
            stat.setTotalSales(stat.getTotalSales() + event.getTotalSales());
            stat.setOrderCount(stat.getOrderCount() + 1);
            stat.setItemCount(stat.getItemCount() + event.getTotalQuantity());
            log.info("주문 완료 stat totalsales:{}, orderCount:{}, itemCount:{} ", stat.getTotalSales(), stat.getOrderCount(), stat.getItemCount());
        }
        table.putItem(stat);
    }

    //주문취소시 테이블에서 마이너스 
    @KafkaListener(topics = "order-canceled-st", groupId = "stats-service")
    public void handleOrderCanceledEvent(String message) {
        try {
            // 1. JSON -> DTO 변환
            OrderEventDto event = objectMapper.readValue(message, OrderEventDto.class);

            // 2. 날짜별 집계키 생성
            LocalDateTime createdAt = event.getCreatedAt();

            // 일간: daily, yyyy-MM-dd
            updateStatMinus("daily", createdAt.format(DateTimeFormatter.ISO_DATE), event);

            // 주간: weekly, yyyy-ww (ISO 기준)
            int week = createdAt.toLocalDate().get(WeekFields.ISO.weekOfWeekBasedYear());
            String weekKey = String.format("%d-%02d", createdAt.getYear(), week);
            updateStatMinus("weekly", weekKey, event);

            // 월간: monthly, yyyy-MM
            updateStatMinus("monthly", createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM")), event);
            log.info("OrderEventCanceled event :{}", event);
        } catch (Exception e) {
            log.error("OrderEventListener 취소 처리 중 오류", e);
        }
    }

    private void updateStatMinus(String dateType, String dateKey, OrderEventDto event) {
        DynamoDbTable<SalesStat> table = enhancedClient.table("SalesStat", TableSchema.fromBean(SalesStat.class));
        SalesStat stat = table.getItem(Key.builder().partitionValue(dateType).sortValue(dateKey).build());
        if (stat != null) {
            stat.setTotalSales(stat.getTotalSales() - event.getTotalSales());
            stat.setOrderCount(stat.getOrderCount() - 1);
            stat.setItemCount(stat.getItemCount() - event.getTotalQuantity());
            log.info("주문 취소 처리 stat totalsales:{}, orderCount:{}, itemCount:{} ", stat.getTotalSales(), stat.getOrderCount(), stat.getItemCount());
            // 음수 방지 (선택)
            if (stat.getTotalSales() < 0) stat.setTotalSales(0L);
            if (stat.getOrderCount() < 0) stat.setOrderCount(0L);
            if (stat.getItemCount() < 0) stat.setItemCount(0L);
            table.putItem(stat);
        }
    }
}
