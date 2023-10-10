package com.sky.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.sky.entity.Setmeal;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * ClassName: SetmealMapper
 * Package: com.sky.mapper
 */
@Mapper
public interface SetmealMapper extends MPJBaseMapper<Setmeal> {
    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
