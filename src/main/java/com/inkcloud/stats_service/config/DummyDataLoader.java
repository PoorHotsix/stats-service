// package com.inkcloud.stats_service.config;

// import com.inkcloud.stats_service.domain.SalesStat;
// import lombok.RequiredArgsConstructor;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;
// import software.amazon.awssdk.enhanced.dynamodb.*;

// import java.util.ArrayList;
// import java.util.List;

// @Component
// @RequiredArgsConstructor
// public class DummyDataLoader implements CommandLineRunner {

//     private final DynamoDbEnhancedClient enhancedClient;

//     @Override
//     public void run(String... args) {
//         DynamoDbTable<SalesStat> table = enhancedClient.table("SalesStat", TableSchema.fromBean(SalesStat.class));

//         List<SalesStat> dummyList = new ArrayList<>();

//         // 일간 데이터 50개 (2025-06-01 ~ 2025-07-20)
//         for (int i = 1; i <= 50; i++) {
//             String day = String.format("2025-06-%02d", i <= 30 ? i : i - 30);
//             String month = i <= 30 ? "06" : "07";
//             String dateKey = "2025-" + month + "-" + String.format("%02d", i <= 30 ? i : i - 30);
//             dummyList.add(new SalesStat(
//                 "daily",
//                 dateKey,
//                 10000L + i * 500,
//                 2L + (i % 5),
//                 5L + (i % 10)
//             ));
//         }

//         // 주간 데이터 10개 (2025-20 ~ 2025-29)
//         for (int i = 20; i < 30; i++) {
//             dummyList.add(new SalesStat(
//                 "weekly",
//                 String.format("2025-%02d", i),
//                 90000L + i * 3000,
//                 10L + (i % 7),
//                 30L + (i % 15)
//             ));
//         }

//         // 월간 데이터 6개 (2025-01 ~ 2025-06)
//         for (int i = 1; i <= 6; i++) {
//             dummyList.add(new SalesStat(
//                 "monthly",
//                 String.format("2025-%02d", i),
//                 300000L + i * 20000,
//                 40L + i * 3,
//                 120L + i * 10
//             ));
//         }

//         for (SalesStat stat : dummyList) {
//             table.putItem(stat);
//         }
//         System.out.println("더미 데이터 입력 완료 (" + dummyList.size() + "건)");
//     }
// }