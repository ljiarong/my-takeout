package com.sky.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.sky.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * ClassName: DishMapper
 * Package: com.sky.mapper
 */
@Mapper
public interface DishMapper extends MPJBaseMapper<Dish> {
    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

}
