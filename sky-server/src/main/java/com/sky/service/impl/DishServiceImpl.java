package com.sky.service.impl;/**
 * ClassName: DishServiceImpl
 * Package: com.sky.service.impl
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.query.MPJLambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.*;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.DishVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-09-28 23:45
 **/
@Service
public class DishServiceImpl extends ServiceImpl<DishFlavorMapper,DishFlavor> implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Override
    @Transactional
    public void addDish(DishDTO dishDTO) {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
//        dish.setCreateTime(LocalDateTime.now());
//        dish.setCreateUser(BaseContext.getCurrentId());
//        dish.setUpdateTime(LocalDateTime.now());
//        dish.setUpdateUser(BaseContext.getCurrentId());
        dish.setStatus(StatusConstant.DISABLE);
        dishMapper.insert(dish);
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        saveBatch(flavors);
    }

    @Override
    public PageResult getDishPage(DishPageQueryDTO dishPageQueryDTO) {
        int page = dishPageQueryDTO.getPage();
        int pageSize = dishPageQueryDTO.getPageSize();
        IPage<DishVO> dishVOIPage=new Page<>(page,pageSize);
        MPJLambdaWrapper<Dish> dishMPJLambdaWrapper=new MPJLambdaWrapper<>();
        dishMPJLambdaWrapper
                .selectAll(Dish.class)
                .like(StringUtils.isNotBlank(dishPageQueryDTO.getName()),Dish::getName,dishPageQueryDTO.getName())
                .eq(dishPageQueryDTO.getCategoryId()!=null,Dish::getCategoryId,dishPageQueryDTO.getCategoryId())
                .eq(dishPageQueryDTO.getStatus()!=null,Dish::getStatus,dishPageQueryDTO.getStatus())
                .selectAs(Category::getName,DishVO::getCategoryName)
                        .leftJoin(Category.class,Category::getId,Dish::getCategoryId);


        dishVOIPage = dishMapper.selectJoinPage(dishVOIPage, DishVO.class, dishMPJLambdaWrapper);
        return new PageResult(dishVOIPage.getTotal(),dishVOIPage.getRecords());
    }

    @Override
    public void deleteDishById(List<Long> ids) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(!CollectionUtils.isEmpty(ids),Dish::getId,ids);
        List<Dish> dishes = dishMapper.selectList(dishLambdaQueryWrapper);
        for (Dish dish : dishes) {
            if (dish.getStatus().equals(StatusConstant.ENABLE))
            {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(!CollectionUtils.isEmpty(ids),SetmealDish::getDishId,ids);
        Long count = setmealDishMapper.selectCount(setmealDishLambdaQueryWrapper);
        if (count>0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        else {
            dishMapper.deleteBatchIds(ids);
            LambdaQueryWrapper<DishFlavor> dishFlavorQueryWrapper=new LambdaQueryWrapper<>();
            dishFlavorQueryWrapper.in(!CollectionUtils.isEmpty(ids),DishFlavor::getDishId,ids);
            dishFlavorMapper.delete(dishFlavorQueryWrapper);
        }
    }

    @Override
    public DishVO getDishByid(Long id) {
        Dish dish = dishMapper.selectById(id);
        DishVO dishVO=new DishVO();
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> dishFlavors = dishFlavorMapper.selectList(dishFlavorLambdaQueryWrapper);
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    @Override
    @Transactional
    public void updateDish(DishDTO dishDTO) {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.updateById(dish);
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dishDTO.getId());
        dishFlavorMapper.delete(dishFlavorLambdaQueryWrapper);
        List<DishFlavor> dishFlavorList= dishDTO.getFlavors();
        if(!CollectionUtils.isEmpty(dishFlavorList)){
            for (DishFlavor flavor : dishFlavorList) {
                flavor.setDishId(dishDTO.getId());
            }
        }
        saveBatch(dishFlavorList);
    }

    @Override
    public void updateState(Long id, Integer status) {
        Dish dish= Dish.builder().id(id).status(status).build();
        dishMapper.updateById(dish);
        if(status.equals(StatusConstant.DISABLE)){
            LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper=
                    new LambdaQueryWrapper<>();
            setmealDishLambdaQueryWrapper.select(SetmealDish::getSetmealId)
                    .eq(SetmealDish::getDishId,id);
            List<Object> list = setmealDishMapper.selectObjs(setmealDishLambdaQueryWrapper);
            for (Object o : list) {
                Long o1 = (Long) o;
                Setmeal setmeal= Setmeal.builder().id(o1).status(StatusConstant.DISABLE).build();
                setmealMapper.updateById(setmeal);
            }
        }
    }

    @Override
    public List<Dish> getDishByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,categoryId);
        dishLambdaQueryWrapper.eq(Dish::getStatus,StatusConstant.ENABLE);//状态要启售的
        List<Dish> dishList = dishMapper.selectList(dishLambdaQueryWrapper);
        return dishList;
    }

    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        MPJLambdaWrapper<Dish> dishMPJLambdaWrapper=new MPJLambdaWrapper<>();
        dishMPJLambdaWrapper
                .selectAll(Dish.class)
                .eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId())
                .eq(dish.getStatus()!=null,Dish::getStatus,dish.getStatus())
                .selectAs(Category::getName,DishVO::getCategoryName)
                .leftJoin(Category.class,Category::getId,Dish::getCategoryId);


        List<DishVO> list = dishMapper.selectJoinList(DishVO.class,dishMPJLambdaWrapper);
        for (DishVO dishVO : list) {
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dishVO.getId());
            List<DishFlavor> dishFlavors = dishFlavorMapper.selectList(dishFlavorLambdaQueryWrapper);
            dishVO.setFlavors(dishFlavors);
        }
        return list;
    }
}
