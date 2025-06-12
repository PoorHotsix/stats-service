package com.inkcloud.stats_service.service;

import java.time.LocalDate;
import java.util.List;
import com.inkcloud.stats_service.domain.SalesStat;

public interface StatsService {
    // 일별 매출 현황 조회 (start~end 구간)

    List<SalesStat> getSalesStats(String type, LocalDate start, LocalDate end);
}
