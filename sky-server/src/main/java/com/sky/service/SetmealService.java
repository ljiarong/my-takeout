package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

/**
 * ClassName: SetmealService
 * Package: com.sky.service
 */
public interface SetmealService extends IService<SetmealDish> {
    void addSetmeal(SetmealDTO setmealDTO);

    PageResult getSetmealPage(SetmealPageQueryDTO setmealPageQueryDTO);

    void deleteSetmeal(List<Long> ids);

    SetmealVO getSetmealById(Long id);

    void updateSetmeal(SetmealDTO setmealDTO);

    void updateStatus(Long id, Integer status);

    List<Setmeal> getListByCategoryId(Setmeal setmeal);

    List<DishItemVO> getDishItemById(Long id);
}
