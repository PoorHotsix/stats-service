package com.inkcloud.stats_service.config;

import com.inkcloud.stats_service.domain.SalesStat;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DummyDataLoader implements CommandLineRunner {

    private final DynamoDbEnhancedClient enhancedClient;

    @Override
    public void run(String... args) {
        DynamoDbTable<SalesStat> table = enhancedClient.table("SalesStat", TableSchema.fromBean(SalesStat.class));

        List<SalesStat> dailyList = new ArrayList<>();
        List<SalesStat> weeklyList = new ArrayList<>();
        List<SalesStat> monthlyList = new ArrayList<>();

        java.time.LocalDate endDate = java.time.LocalDate.now();
        java.time.LocalDate startDate = endDate.minusMonths(3).plusDays(1);

        long baseSales = 10000L;
        long sales = baseSales;
        long orderCount = 2L;
        long itemCount = 5L;

        java.util.Random random = new java.util.Random(42);

        // 일간 데이터 생성
        List<Long> dailySalesHistory = new ArrayList<>();
        List<Long> dailyOrderHistory = new ArrayList<>();
        List<Long> dailyItemHistory = new ArrayList<>();

        for (java.time.LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            long fluctuation = (long) (random.nextGaussian() * 2000);
            sales += 500 + fluctuation;
            if (sales < baseSales) sales = baseSales;

            orderCount += random.nextInt(3) - 1;
            if (orderCount < 1) orderCount = 1;

            itemCount += random.nextInt(5) - 2;
            if (itemCount < 1) itemCount = 1;

            dailySalesHistory.add(sales);
            dailyOrderHistory.add(orderCount);
            dailyItemHistory.add(itemCount);

            dailyList.add(new SalesStat(
                "daily",
                date.toString(),
                sales,
                orderCount,
                itemCount
            ));
        }

        // 주간 데이터 생성
        java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.ISO;
        Map<String, List<Integer>> weekIndexMap = new LinkedHashMap<>();
        int idx = 0;
        for (java.time.LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1), idx++) {
            int week = date.get(weekFields.weekOfWeekBasedYear());
            String weekKey = String.format("%d-%02d", date.getYear(), week);
            weekIndexMap.computeIfAbsent(weekKey, k -> new ArrayList<>()).add(idx);
        }
        for (Map.Entry<String, List<Integer>> entry : weekIndexMap.entrySet()) {
            long sumSales = 0, sumOrder = 0, sumItem = 0;
            for (int i : entry.getValue()) {
                sumSales += dailySalesHistory.get(i);
                sumOrder += dailyOrderHistory.get(i);
                sumItem += dailyItemHistory.get(i);
            }
            weeklyList.add(new SalesStat(
                "weekly",
                entry.getKey(),
                sumSales,
                sumOrder,
                sumItem
            ));
        }

        // 월간 데이터 생성
        Map<String, List<Integer>> monthIndexMap = new LinkedHashMap<>();
        idx = 0;
        for (java.time.LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1), idx++) {
            String monthKey = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
            monthIndexMap.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(idx);
        }
        for (Map.Entry<String, List<Integer>> entry : monthIndexMap.entrySet()) {
            long sumSales = 0, sumOrder = 0, sumItem = 0;
            for (int i : entry.getValue()) {
                sumSales += dailySalesHistory.get(i);
                sumOrder += dailyOrderHistory.get(i);
                sumItem += dailyItemHistory.get(i);
            }
            monthlyList.add(new SalesStat(
                "monthly",
                entry.getKey(),
                sumSales,
                sumOrder,
                sumItem
            ));
        }

        // 데이터 저장
        for (SalesStat stat : dailyList) table.putItem(stat);
        for (SalesStat stat : weeklyList) table.putItem(stat);
        for (SalesStat stat : monthlyList) table.putItem(stat);

        System.out.println("더미 데이터 입력 완료 (일간: " + dailyList.size() +
                " / 주간: " + weeklyList.size() +
                " / 월간: " + monthlyList.size() + ")");
    }
}