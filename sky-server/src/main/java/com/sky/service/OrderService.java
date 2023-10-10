package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.*;
import com.sky.entity.OrderDetail;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

/**
 * ClassName: OrderService
 * Package: com.sky.service
 */
public interface OrderService extends IService<OrderDetail> {
    OrderSubmitVO orderSubmit(OrdersSubmitDTO ordersSubmitDTO);

    void paySuccess(String outTradeNo);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    PageResult getorders(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO getOrder(Long id);

    void cancelOrder(Long id) throws Exception;

    void oneMoreOrder(Long id);

    PageResult getOrdersCondition(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO getOrdersStatus();

    OrderVO getOrderDetail(Long id);

    void confirmOrder(Long id);

    void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    void cancelOrderAdmin(OrdersCancelDTO ordersCancelDTO) throws Exception;

    void deliveryOrder(Long id);

    void completeOrder(Long id);

    void reminder(Long id);
}
