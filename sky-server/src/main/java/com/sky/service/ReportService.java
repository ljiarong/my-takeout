package com.sky.service;

import com.sky.vo.*;

import java.time.LocalDate;

/**
 * ClassName: ReportService
 * Package: com.sky.service
 */
public interface ReportService {
    TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end);

    UserReportVO getUserReport(LocalDate begin, LocalDate end);

    OrderReportVO getOrderReport(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end);

}
