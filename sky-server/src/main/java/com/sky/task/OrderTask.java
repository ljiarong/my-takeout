package com.sky.task;/**
 * ClassName: OrderTask
 * Package: com.sky.task
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-10-09 21:18
 **/
@Slf4j
@Component
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * ?")  //每分钟执行一次
    @Transactional
    public void processTimeoutOrder(){
        log.info("处理支付超时订单：{}", new Date());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> orderList = getOrdersByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
        for (Orders orders : orderList) {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("支付超时，自动取消");
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.updateById(orders);
        }
    }

    /**
     * 处理“派送中”状态的订单
     */
    @Scheduled(cron = "0 0 */1 * * ?")  //每小时触发一次
    @Transactional
    public void processDeliveryOrder(){
        log.info("处理派送中订单：{}", new Date());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> orderList = getOrdersByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        for (Orders orders : orderList) {
            orders.setStatus(Orders.COMPLETED);
            orders.setDeliveryTime(LocalDateTime.now());
            //deliveryStatus是送出时间的方式，不是派送状态
            orderMapper.updateById(orders);
        }
    }
    private List<Orders> getOrdersByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime){
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper=new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getStatus,status)
                .lt(Orders::getOrderTime,orderTime);
        List<Orders> orders = orderMapper.selectList(ordersLambdaQueryWrapper);
        return orders;
    }
}
