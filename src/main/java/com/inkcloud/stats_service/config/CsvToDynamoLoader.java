// package com.inkcloud.stats_service.config;

// import com.inkcloud.stats_service.domain.SalesStat;
// import lombok.RequiredArgsConstructor;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;
// import software.amazon.awssdk.enhanced.dynamodb.*;
// import java.io.*;
// import java.nio.file.*;
// import java.util.*;

// @Component
// @RequiredArgsConstructor
// public class CsvToDynamoLoader implements CommandLineRunner {

//     private final DynamoDbEnhancedClient enhancedClient;

//     @Override
//     public void run(String... args) throws Exception {
//         DynamoDbTable<SalesStat> table = enhancedClient.table("SalesStat", TableSchema.fromBean(SalesStat.class));
//         InputStream is = getClass().getClassLoader().getResourceAsStream("statistics.csv");
//         if (is == null) {
//             throw new FileNotFoundException("statistics.csv not found in resources");
//         }
//         BufferedReader reader = new BufferedReader(new InputStreamReader(is));

//         String line;
//         boolean isFirst = true;

//         while ((line = reader.readLine()) != null) {
//             if (isFirst) {
//                 isFirst = false; // Skip header
//                 continue;
//             }

//             String[] parts = line.split(",");

//             if (parts.length != 5) {
//                 System.out.println("⚠️ 잘못된 행 무시: " + line);
//                 continue;
//             }

//             SalesStat stat = new SalesStat(
//                     parts[0].trim(), // dateType
//                     parts[1].trim(), // dateKey
//                     Long.parseLong(parts[2].trim()), // totalSales
//                     Long.parseLong(parts[3].trim()), // orderCount
//                     Long.parseLong(parts[4].trim())  // itemCount
//             );

//             table.putItem(stat);
//         }

//         System.out.println("✅ CSV -> DynamoDB 적재 완료!");
//     }
// }
