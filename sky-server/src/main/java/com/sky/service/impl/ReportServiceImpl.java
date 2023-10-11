package com.sky.service.impl;/**
 * ClassName: ReportServiceImpl
 * Package: com.sky.service.impl
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-10-10 16:08
 **/
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end) {
        log.info("ReportServiceImpl的getTurnoverReport方法执行中，参数为{}"+begin+end);
        List<LocalDate> localDates=new ArrayList<>();
        localDates.add(begin);
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            localDates.add(begin);
        }
        List<Double> turnoverList=new ArrayList<>();
        for (LocalDate date : localDates) {
            LocalDateTime beginDayTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endDayTime = LocalDateTime.of(date, LocalTime.MAX);
            Object o = getTurnoverByTimeAndStatus(beginDayTime, endDayTime, null,"sum(amount) as turnover");
            Double turnover=(o==null? 0.0 : Double.parseDouble(((BigDecimal) o).toString()));
            turnoverList.add(turnover);
        }
        return TurnoverReportVO.builder().turnoverList(StringUtils.join(turnoverList,","))
                .dateList(StringUtils.join(localDates,",")).build();
    }

    @Override
    public UserReportVO getUserReport(LocalDate begin, LocalDate end) {
        List<LocalDate> localDates=new ArrayList<>();
        localDates.add(begin);
        while (!begin.equals(end)){
             begin=begin.plusDays(1);
             localDates.add(begin);
        }
        List<Long> totalUser=new ArrayList<>();
        List<Long> userNewAdd=new ArrayList<>();
        for (LocalDate localDate : localDates) {
            LocalDateTime beginDayTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endDayTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Long totalNum = getUserNum(null, endDayTime);
            Long userNewAddDay = getUserNum(beginDayTime, endDayTime);
            userNewAdd.add(userNewAddDay);
            totalUser.add(totalNum);
        }
        return UserReportVO.builder().dateList(StringUtils.join(localDates,","))
                .newUserList(StringUtils.join(userNewAdd,","))
                .totalUserList(StringUtils.join(totalUser,",")).build();
    }

    @Override
    public OrderReportVO getOrderReport(LocalDate begin, LocalDate end) {
        List<LocalDate> localDates=new ArrayList<>();
        localDates.add(begin);
        while (!begin.equals(end)) {
            begin=begin.plusDays(1);
            localDates.add(begin);
        }
        List<Integer> totalOrderCount=new ArrayList<>();
        List<Integer> validOrderCount=new ArrayList<>();
        for (LocalDate date : localDates) {
            LocalDateTime beginDayTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endDayTime = LocalDateTime.of(date, LocalTime.MAX);
            Object valido = getTurnoverByTimeAndStatus(beginDayTime, endDayTime, Orders.COMPLETED, "count(*) as orderCount");
            Integer validOrderCountDay = Integer.parseInt(valido.toString());
            validOrderCount.add(validOrderCountDay);
            Object totalo = getTurnoverByTimeAndStatus(beginDayTime, endDayTime, null, "count(*) as orderCount");
            Integer totalOrderCountDay = Integer.parseInt(totalo.toString());
            totalOrderCount.add(totalOrderCountDay);
        }
        Integer totalOrderCountNum = totalOrderCount.stream().reduce(Integer::sum).get();
        //时间区间内的总有效订单数
        Integer validOrderCountNum = validOrderCount.stream().reduce(Integer::sum).get();
        //订单完成率
        Double orderCompletionRate = 0.0;
        if(totalOrderCountNum != 0){
            orderCompletionRate = validOrderCountNum.doubleValue() / totalOrderCountNum;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(localDates, ","))
                .orderCountList(StringUtils.join(totalOrderCount, ","))
                .validOrderCountList(StringUtils.join(validOrderCount, ","))
                .totalOrderCount(totalOrderCountNum)
                .validOrderCount(validOrderCountNum)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        MPJLambdaWrapper<OrderDetail> orderDetailMPJLambdaWrapper=new MPJLambdaWrapper<>();
        orderDetailMPJLambdaWrapper
                .selectAs(OrderDetail::getName,GoodsSalesDTO::getName)
                .selectSum(OrderDetail::getNumber,GoodsSalesDTO::getNumber)
                .ge(begin != null, Orders::getOrderTime, LocalDateTime.of(begin,LocalTime.MIN))
                .le(end != null, Orders::getOrderTime, LocalDateTime.of(end,LocalTime.MAX))
                .eq(Orders::getStatus,Orders.COMPLETED)
                .groupBy(OrderDetail::getName)
//                .last("limit 10")
                .leftJoin(Orders.class,Orders::getId,OrderDetail::getOrderId);
//                .orderByDesc(GoodsSalesDTO::getNumber);
//        IPage<GoodsSalesDTO> goodsSalesDTOIPage=new Page<>(1,10);    //top10
//        goodsSalesDTOIPage= orderDetailMapper.selectJoinPage(goodsSalesDTOIPage,GoodsSalesDTO.class, orderDetailMPJLambdaWrapper);
//        List<GoodsSalesDTO> goodsSalesDTOS = goodsSalesDTOIPage.getRecords();
        List<GoodsSalesDTO> goodsSalesDTOS = orderDetailMapper.selectJoinList(GoodsSalesDTO.class, orderDetailMPJLambdaWrapper);
        List<GoodsSalesDTO> listSorted = goodsSalesDTOS.stream().sorted(Comparator.comparing(GoodsSalesDTO::getNumber).reversed()).collect(Collectors.toList());
        List<String> dishNameList=new ArrayList<>();
        List<Integer> numberList=new ArrayList<>();
        if (listSorted.size()>10){
            listSorted=listSorted.subList(0,10);
        }
        for (GoodsSalesDTO goodsSalesDTO : listSorted) {
            dishNameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber());
        }
        return SalesTop10ReportVO.builder().numberList(StringUtils.join(numberList,","))
                .nameList(StringUtils.join(dishNameList,",")).build();
    }

    @Override
    public void getReport(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        //查询概览运营数据，提供给Excel模板文件
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin,LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            //基于提供好的模板文件创建一个新的Excel表格对象
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //获得Excel文件中的一个Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            sheet.getRow(1).getCell(1).setCellValue(begin + "至" + end);
            //获得第4行
            XSSFRow row = sheet.getRow(3);
            //获取单元格
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                //准备明细数据
                businessData = workspaceService.getBusinessData(LocalDateTime.of(date,LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }
            //通过输出流将文件下载到客户端浏览器中
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            //关闭资源
            out.flush();
            out.close();
            excel.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }


    private Object getTurnoverByTimeAndStatus(LocalDateTime begin,LocalDateTime end,Integer status,String queryString){
        QueryWrapper<Orders> ordersQueryWrapper=new QueryWrapper<>();
        List<Object> objs = orderMapper.selectObjs(ordersQueryWrapper.select(queryString)
                .lambda()
                .ge(begin != null, Orders::getOrderTime, begin)
                .le(end != null, Orders::getOrderTime, end)
                .eq(status != null, Orders::getStatus, status));
        Object o = objs.get(0);
        return o;

    }
    private Long getUserNum(LocalDateTime begin,LocalDateTime end){
        QueryWrapper<User> userQueryWrapper=new QueryWrapper<>();
        List<Object> objs = userMapper.selectObjs(userQueryWrapper.select("count(*) as userNum")
                .lambda()
                .ge(begin != null, User::getCreateTime, begin)
                .le(end != null, User::getCreateTime, end));
        Object o = objs.get(0);   //这里不会为空值是因为count没有行的情况会等于0
        return Long.parseLong(o.toString());
    }

}
