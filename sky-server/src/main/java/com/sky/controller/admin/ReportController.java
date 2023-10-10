package com.sky.controller.admin;/**
 * ClassName: ReportController
 * Package: com.sky.controller.admin
 */

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-10-10 16:02
 **/
@RestController
@RequestMapping("admin/report")
@Slf4j
public class ReportController {
    @Autowired
    private ReportService reportService;
    @GetMapping("turnoverStatistics")
    public Result<TurnoverReportVO> getTurnoverReport(
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end){
        TurnoverReportVO turnoverReportVO=reportService.getTurnoverReport(begin,end);
        return Result.success(turnoverReportVO);
    }
    @GetMapping("userStatistics")
    public Result<UserReportVO> getUserReport(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern ="yyyy-MM-dd") LocalDate end){
        log.info("ReportController的getUserReport方法执行中，参数为{}"+begin+end);
        UserReportVO userReportVO=reportService.getUserReport(begin,end);
        return Result.success(userReportVO);
    }
    @GetMapping("ordersStatistics")
    public Result<OrderReportVO> getOrderReport(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern ="yyyy-MM-dd") LocalDate end){
        log.info("ReportController的getOrderReport方法执行中，参数为{}"+begin+end);
        OrderReportVO orderReportVO=reportService.getOrderReport(begin,end);
        return Result.success(orderReportVO);
    }
    @GetMapping("top10")
    public Result<SalesTop10ReportVO> getTop10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern ="yyyy-MM-dd") LocalDate end){
        log.info("ReportController的getTop10方法执行中，参数为{}"+begin+end);
        SalesTop10ReportVO salesTop10ReportVO=reportService.getTop10(begin,end);
        return Result.success(salesTop10ReportVO);
    }
}
