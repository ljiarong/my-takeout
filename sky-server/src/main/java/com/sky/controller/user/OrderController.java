package com.sky.controller.user;/**
 * ClassName: OrderController
 * Package: com.sky.controller.user
 */

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-10-08 18:44
 **/
@RestController
@RequestMapping("user/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    @PostMapping("submit")
    public Result<OrderSubmitVO> orderSubmit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("OrderController的orderSubmit方法执行中，参数为{}",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO=orderService.orderSubmit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @PutMapping("/payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }
    @GetMapping("historyOrders")
    public Result<PageResult> getOrders(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("OrderController的getOrders方法执行中，参数为{}",ordersPageQueryDTO);
        PageResult pageResult=orderService.getorders(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("orderDetail/{id}")
    public Result<OrderVO> getOrder(@PathVariable(value = "id") Long id){
        log.info("OrderController的getOrder方法执行中，参数为{}",id);
        OrderVO orderVO=orderService.getOrder(id);
        return Result.success(orderVO);
    }

    @PutMapping("cancel/{id}")
    public Result cancelOrder(@PathVariable(value = "id") Long id) throws Exception{
        log.info("OrderController的cancelOrder方法执行中，参数为{}",id);
        orderService.cancelOrder(id);
        return Result.success();
    }

    @PostMapping("repetition/{id}")
    public Result oneMoreOrder(@PathVariable(value = "id") Long id){
        log.info("OrderController的oneMoreOrder方法执行中，参数为{}",id);
        orderService.oneMoreOrder(id);
        return Result.success();
    }

    @GetMapping("reminder/{id}")
    public Result reminder(@PathVariable(value = "id") Long id){
        log.info("OrderController的reminder方法执行中，参数为{}",id);
        orderService.reminder(id);
        return Result.success();
    }
}
