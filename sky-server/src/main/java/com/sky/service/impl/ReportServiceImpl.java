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
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    @Override    //TODO:无法识别GoodsSalesDto类
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        MPJLambdaWrapper<OrderDetail> orderDetailMPJLambdaWrapper=new MPJLambdaWrapper<>();
        orderDetailMPJLambdaWrapper
                .selectAs(OrderDetail::getName,GoodsSalesDTO::getName)
                .selectAs("sum(number)",GoodsSalesDTO::getNumber)
                .ge(begin != null, Orders::getOrderTime, begin)
                .le(end != null, Orders::getOrderTime, end)
                .eq(Orders::getStatus,Orders.COMPLETED)
                .groupBy(OrderDetail::getName)
                .leftJoin(Orders.class,Orders::getId,OrderDetail::getOrderId)
                .orderByDesc(GoodsSalesDTO::getNumber);
        List<GoodsSalesDTO> goodsSalesDTOS = orderDetailMapper.selectJoinList(GoodsSalesDTO.class, orderDetailMPJLambdaWrapper);
        List<String> dishNameList=new ArrayList<>();
        List<Integer> numberList=new ArrayList<>();
        for (GoodsSalesDTO goodsSalesDTO : goodsSalesDTOS) {
            dishNameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber());
        }
        return SalesTop10ReportVO.builder().numberList(StringUtils.join(numberList,","))
                .nameList(StringUtils.join(dishNameList,",")).build();
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
