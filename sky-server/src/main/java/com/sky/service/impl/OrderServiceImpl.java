package com.sky.service.impl;/**
 * ClassName: OrderServiceImpl
 * Package: com.sky.service.impl
 */

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.server.WebSocketServer;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-10-08 18:49
 **/
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderDetailMapper,OrderDetail> implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WebSocketServer webSocketServer;
    @Override
    @Transactional
    public OrderSubmitVO orderSubmit(OrdersSubmitDTO ordersSubmitDTO) {
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.selectById(addressBookId);
        if(addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();

        //购物车数据
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper=new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectList(shoppingCartLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(shoppingCartList)) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        Orders orders=new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orderMapper.insert(orders);
        Long ordersId = orders.getId();

        List<OrderDetail> orderDetails=new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(ordersId);
            orderDetails.add(orderDetail);
        }
        saveBatch(orderDetails);
        shoppingCartMapper.delete(shoppingCartLambdaQueryWrapper);


        OrderSubmitVO submitVO = OrderSubmitVO.builder().orderAmount(orders.getAmount()).orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime()).id(ordersId).build();

        return submitVO;
    }

    @Override
    public void paySuccess(String outTradeNo) {
        // 根据订单号查询订单
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper=new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getNumber,outTradeNo);
        Orders ordersDB = orderMapper.selectOne(ordersLambdaQueryWrapper);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.updateById(orders);

        JSONObject jsonObject=new JSONObject();
        jsonObject.put("type",1);
        jsonObject.put("orderId",orders.getId());
        jsonObject.put("content","订单号:"+orders.getNumber());
        webSocketServer.sendToAllClient(jsonObject.toJSONString());
    }

    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    @Override
    public PageResult getorders(OrdersPageQueryDTO ordersPageQueryDTO) {
        IPage<Orders> ordersIPage=new Page<>(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper=new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getUserId,BaseContext.getCurrentId())
                .eq(ordersPageQueryDTO.getStatus()!=null,Orders::getStatus,ordersPageQueryDTO.getStatus());
        ordersIPage = orderMapper.selectPage(ordersIPage, ordersLambdaQueryWrapper);


        List<OrderVO> orderVOS=new ArrayList<>();
        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper=new LambdaQueryWrapper<>();
        for (Orders record : ordersIPage.getRecords()) {
            OrderVO orderVO=new OrderVO();
            BeanUtils.copyProperties(record,orderVO);
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,orderVO.getId());
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(orderDetailLambdaQueryWrapper);
            orderVO.setOrderDetailList(orderDetails);
            orderVOS.add(orderVO);
        }
        return new PageResult(ordersIPage.getTotal(),orderVOS);


    }

    @Override
    public OrderVO getOrder(Long id) {
        return getOrderVoByOrderId(id);      //TODO:所有用户端根据订单id查询的接口可以再加一下userid判断
    }

    @Override
    public void cancelOrder(Long id) throws Exception {
        Orders orders = orderMapper.selectById(id);
        if (orders==null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Integer status = orders.getStatus();
        if (status.equals(Orders.PENDING_PAYMENT)||status.equals(Orders.TO_BE_CONFIRMED)) {
            if (status.equals(Orders.TO_BE_CONFIRMED)){
                weChatPayUtil.refund(orders.getNumber(),  //商户订单号
                        orders.getNumber(),   //商户退款单号
                        new BigDecimal(0.01),  //退款金额
                        new BigDecimal(0.01));  //原订单金额
                orders.setPayStatus(Orders.REFUND);
            }  //需要进行退款
            orders.setCancelTime(LocalDateTime.now());
            orders.setCancelReason("用户取消");
            orders.setStatus(Orders.CANCELLED);
            orderMapper.updateById(orders);
        } else if (status.equals(Orders.CONFIRMED)||status.equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        else {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

    }

    @Override
    @Transactional
    public void oneMoreOrder(Long id) {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper=new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,id);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(orderDetailLambdaQueryWrapper);
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart=new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart,"id");
            shoppingCart.setUserId(userId);
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public PageResult getOrdersCondition(OrdersPageQueryDTO ordersPageQueryDTO) {
        IPage<Orders> ordersIPage = orderConditionIfNotNull(ordersPageQueryDTO);
        List<Orders> records = ordersIPage.getRecords();
        List<OrderVO> orderVOS=new ArrayList<>();
        for (Orders record : records) {
            OrderVO orderVO=new OrderVO();
            BeanUtils.copyProperties(record,orderVO);
            String orderDishesStr = getOrderDishesStr(record.getId());
            orderVO.setOrderDishes(orderDishesStr);
            orderVOS.add(orderVO);
        }
        return new PageResult(ordersIPage.getTotal(),orderVOS);

    }

    @Override
    public OrderStatisticsVO getOrdersStatus() {
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper=new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getStatus,Orders.CONFIRMED);
        Long confirmed = orderMapper.selectCount(ordersLambdaQueryWrapper);
        ordersLambdaQueryWrapper.clear();
        ordersLambdaQueryWrapper.eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS);
        Long deliveryInProgress = orderMapper.selectCount(ordersLambdaQueryWrapper);
        ordersLambdaQueryWrapper.clear();
        ordersLambdaQueryWrapper.eq(Orders::getStatus,Orders.TO_BE_CONFIRMED);
        Long toBeConfirmed = orderMapper.selectCount(ordersLambdaQueryWrapper);
        OrderStatisticsVO orderStatisticsVO=new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed.intValue());
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress.intValue());
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed.intValue());
        return orderStatisticsVO;
    }

    @Override
    public OrderVO getOrderDetail(Long id) {
        return getOrderVoByOrderId(id);
    }

    @Override
    public void confirmOrder(Long id) {
        Orders orders = Orders.builder().id(id).status(Orders.CONFIRMED).build();
        orderMapper.updateById(orders);
    }

    @Override
    public void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Orders orders1 = orderMapper.selectById(ordersRejectionDTO.getId());
        if ((orders1==null || !orders1.getStatus().equals(Orders.TO_BE_CONFIRMED))) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if (orders1.getPayStatus().equals(Orders.PAID)) {
            String refund = weChatPayUtil.refund(
                    orders1.getNumber(),
                    orders1.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("OrderServiceImpl的rejectionOrder方法执行中，参数为{申请退款}",refund);
        }
        Orders orders = Orders.builder().id(ordersRejectionDTO.getId()).status(Orders.CANCELLED).rejectionReason(ordersRejectionDTO.getRejectionReason()).cancelTime(LocalDateTime.now()).build();
        orderMapper.updateById(orders);
    }

    @Override
    public void cancelOrderAdmin(OrdersCancelDTO ordersCancelDTO) throws Exception {
        Orders orders = orderMapper.selectById(ordersCancelDTO.getId());
        Integer payStatus = orders.getPayStatus();
        if (payStatus.equals(Orders.PAID)) {
            String refund = weChatPayUtil.refund(
                    orders.getNumber(),
                    orders.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("OrderServiceImpl的rejectionOrder方法执行中，参数为{申请退款}",refund);
        }
        Orders orders1 = Orders.builder().id(ordersCancelDTO.getId()).status(Orders.CANCELLED).cancelReason(ordersCancelDTO.getCancelReason()).cancelTime(LocalDateTime.now()).build();
        orderMapper.updateById(orders1);
    }

    @Override
    public void deliveryOrder(Long id) {
        Orders orders = orderMapper.selectById(id);
        if ((orders==null || !orders.getStatus().equals(Orders.CONFIRMED))) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.updateById(orders);
    }

    @Override
    public void completeOrder(Long id) {
        Orders orders = orderMapper.selectById(id);
        if ((orders==null || !orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS))) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.updateById(orders);
    }

    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.selectById(id);
        if (orders==null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        String number = orders.getNumber();
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("type",2);
        jsonObject.put("orderId",id);
        jsonObject.put("content","订单号:"+number);
        webSocketServer.sendToAllClient(jsonObject.toJSONString());
    }

    private IPage<Orders> orderConditionIfNotNull(OrdersPageQueryDTO ordersPageQueryDTO){
        IPage<Orders> ordersIPage=new Page<>(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper=new LambdaQueryWrapper<>();
        LocalDateTime beginTime = ordersPageQueryDTO.getBeginTime();
        LocalDateTime endTime = ordersPageQueryDTO.getEndTime();
        String orderNumber = ordersPageQueryDTO.getNumber();
        String phone = ordersPageQueryDTO.getPhone();
        Integer status = ordersPageQueryDTO.getStatus();
        ordersLambdaQueryWrapper.gt(beginTime !=null,Orders::getOrderTime, beginTime)
                .lt(endTime!=null,Orders::getOrderTime,endTime)
                .eq(StringUtils.isNotBlank(orderNumber),Orders::getNumber,orderNumber)
                .like(StringUtils.isNotBlank(phone),Orders::getPhone,phone)
                .eq(status!=null,Orders::getStatus,status)
                .eq(ordersPageQueryDTO.getUserId()!=null, Orders::getUserId,ordersPageQueryDTO.getUserId());
        return orderMapper.selectPage(ordersIPage, ordersLambdaQueryWrapper);
    }
    private String getOrderDishesStr(Long orderId){
        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper=new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(orderDetailLambdaQueryWrapper);
        List<String> orderDishesStr = orderDetails.stream().map(orderDetail -> {
            String s = orderDetail.getName() + "*" + orderDetail.getNumber() + ";";
            return s;
        }).collect(Collectors.toList());   //订单餐品集合
        return String.join("",orderDishesStr);  //字符串合并
    }
    private OrderVO getOrderVoByOrderId(Long id){
        Orders orders = orderMapper.selectById(id);
        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper=new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,id);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(orderDetailLambdaQueryWrapper);
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }
}
