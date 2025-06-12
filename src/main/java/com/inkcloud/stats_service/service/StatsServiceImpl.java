package com.inkcloud.stats_service.service;

import com.inkcloud.stats_service.domain.SalesStat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final DynamoDbEnhancedClient enhancedClient;

    @Override
    public List<SalesStat> getSalesStats(String dateType, LocalDate start, LocalDate end) {
        DynamoDbTable<SalesStat> table = enhancedClient.table("SalesStat", TableSchema.fromBean(SalesStat.class));
        List<SalesStat> result = new ArrayList<>();
        DateTimeFormatter formatter;

        // dateType에 따라 dateKey 포맷 결정
        switch (dateType) {
            case "daily":
                formatter = DateTimeFormatter.ISO_DATE; // yyyy-MM-dd
                break;
            case "weekly":
                formatter = DateTimeFormatter.ofPattern("yyyy-ww"); // yyyy-ww
                break;
            case "monthly":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM"); // yyyy-MM
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 dateType: " + dateType);
        }

        String startKey, endKey;
        if ("weekly".equals(dateType)) {
            
            int startWeek = start.get(WeekFields.ISO.weekOfWeekBasedYear());
            int endWeek = end.get(WeekFields.ISO.weekOfWeekBasedYear());
            startKey = String.format("%d-%02d", start.getYear(), startWeek);
            endKey = String.format("%d-%02d", end.getYear(), endWeek);

        } else {
            startKey = start.format(formatter);
            endKey = end.format(formatter);
        }

        QueryConditional conditional = QueryConditional
                .sortBetween(
                        Key.builder().partitionValue(dateType).sortValue(startKey).build(),
                        Key.builder().partitionValue(dateType).sortValue(endKey).build()
                );

        table.query(r -> r.queryConditional(conditional))
                .items()
                .forEach(result::add);

        return result;
    }
}
