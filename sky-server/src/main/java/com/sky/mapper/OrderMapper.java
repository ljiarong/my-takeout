package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * ClassName: OrderMapper
 * Package: com.sky.mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
    Double sumByMap(Map map);

    Integer countByMap(Map map);
}
