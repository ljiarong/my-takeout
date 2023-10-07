package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

/**
 * ClassName: DishService
 * Package: com.sky.service
 */
public interface DishService extends IService<DishFlavor> {
    void addDish(DishDTO dishDTO);

    PageResult getDishPage(DishPageQueryDTO dishPageQueryDTO);

    void deleteDishById(List<Long> ids);

    DishVO getDishByid(Long id);

    void updateDish(DishDTO dishDTO);

    void updateState(Long id, Integer status);

    List<Dish> getDishByCategoryId(Long categoryId);

    List<DishVO> listWithFlavor(Dish dish);
}
