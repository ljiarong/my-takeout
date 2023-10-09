package com.sky.controller.admin;/**
 * ClassName: OrderController
 * Package: com.sky.controller.admin
 */

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
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
 * @create: 2023-10-09 16:05
 **/
@RestController("adminOrderController")
@RequestMapping("admin/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    @GetMapping("conditionSearch")
    public Result<PageResult> getOrdersCondition(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("OrderController的getOrdersCondition方法执行中，参数为{}",ordersPageQueryDTO);
        PageResult pageResult=orderService.getOrdersCondition(ordersPageQueryDTO);
        return Result.success(pageResult);

    }
    @GetMapping("statistics")
    public Result<OrderStatisticsVO> getOrdersStatus(){
        log.info("OrderController的getOrdersStatus方法执行中，参数为{}");
        OrderStatisticsVO orderStatisticsVO=orderService.getOrdersStatus();
        return Result.success(orderStatisticsVO);
    }
    @GetMapping("details/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable(value = "id") Long id){
        log.info("OrderController的getOrderDetail方法执行中，参数为{}",id);
        OrderVO orderVO=orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }
    @PutMapping("confirm")
    public Result confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("OrderController的confirmOrder方法执行中，参数为{}", ordersConfirmDTO);
        orderService.confirmOrder(ordersConfirmDTO.getId());
        return Result.success();
    }
    @PutMapping("rejection")
    public Result rejectionOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception{
        log.info("OrderController的rejectionOrder方法执行中，参数为{}",ordersRejectionDTO);
        orderService.rejectionOrder(ordersRejectionDTO);
        return Result.success();
    }
    @PutMapping("cancel")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        log.info("OrderController的cancelOrder方法执行中，参数为{}",ordersCancelDTO);
        orderService.cancelOrderAdmin(ordersCancelDTO);
        return Result.success();
    }
    @PutMapping("delivery/{id}")
    public Result deliveryOrder(@PathVariable(value = "id") Long id){
        log.info("OrderController的deliveryOrder方法执行中，参数为{}", id);
        orderService.deliveryOrder(id);
        return Result.success();
    }
    @PutMapping("complete/{id}")
    public Result completeOrder(@PathVariable(value = "id") Long id){
        log.info("OrderController的completeOrder方法执行中，参数为{}",id);
        orderService.completeOrder(id);
        return Result.success();
    }
}
