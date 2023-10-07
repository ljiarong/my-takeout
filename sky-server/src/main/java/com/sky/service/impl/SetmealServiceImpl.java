package com.sky.service.impl;/**
 * ClassName: SetmealServiceImpl
 * Package: com.sky.service.impl
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-10-06 14:29
 **/
@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealDishMapper,SetmealDish> implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Override
    @Transactional
    public void addSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        saveBatch(setmealDishes);
    }

    @Override
    public PageResult getSetmealPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        IPage<SetmealVO> setmealVOIPage=new Page<>(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        MPJLambdaWrapper<Setmeal> setmealMPJLambdaWrapper=new MPJLambdaWrapper<>();
        setmealMPJLambdaWrapper.selectAll(Setmeal.class)
                .selectAs(Category::getName,SetmealVO::getCategoryName)
                .like(StringUtils.isNotBlank(setmealPageQueryDTO.getName()),Setmeal::getName,setmealPageQueryDTO.getName())
                .eq(setmealPageQueryDTO.getCategoryId()!=null,Setmeal::getCategoryId,setmealPageQueryDTO.getCategoryId())
                .eq(setmealPageQueryDTO.getStatus()!=null,Setmeal::getStatus,setmealPageQueryDTO.getStatus())
                .leftJoin(Category.class,Category::getId,Setmeal::getCategoryId);
        setmealVOIPage=setmealMapper.selectJoinPage(setmealVOIPage,SetmealVO.class,setmealMPJLambdaWrapper);
        return new PageResult(setmealVOIPage.getTotal(),setmealVOIPage.getRecords());
    }

    @Override
    @Transactional
    public void deleteSetmeal(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId,ids);
        List<Setmeal> setmeals = setmealMapper.selectList(setmealLambdaQueryWrapper);
        for (Setmeal setmeal : setmeals) {
            if (setmeal.getStatus().equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        setmealMapper.deleteBatchIds(ids);
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        remove(setmealDishLambdaQueryWrapper);
    }

    @Override
    public SetmealVO getSetmealById(Long id) {
        MPJLambdaWrapper<Setmeal> setmealMPJLambdaWrapper=new MPJLambdaWrapper<>();
        setmealMPJLambdaWrapper.selectAll(Setmeal.class)
                .selectAs(Category::getName,SetmealVO::getCategoryName)
                .eq(Setmeal::getId,id)
                .leftJoin(Category.class,Category::getId,Setmeal::getCategoryId);
        SetmealVO setmealVO = setmealMapper.selectJoinOne(SetmealVO.class, setmealMPJLambdaWrapper);
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(setmealDishLambdaQueryWrapper);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    @Transactional
    public void updateSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmeal.getId());
        }
        setmealMapper.updateById(setmeal);
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        remove(setmealDishLambdaQueryWrapper);
        saveBatch(setmealDishes);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
        if (status.equals(StatusConstant.DISABLE)){
            setmealMapper.updateById(setmeal);
        }
        else if (status.equals(StatusConstant.ENABLE)){
            MPJLambdaWrapper<SetmealDish> setmealDishMPJLambdaWrapper=new MPJLambdaWrapper<>();
            setmealDishMPJLambdaWrapper
                    .select(Dish::getStatus)
                    .eq(SetmealDish::getSetmealId,id)
                    .leftJoin(Dish.class,Dish::getId,SetmealDish::getDishId);
            List<Object> objects = setmealDishMapper.selectObjs(setmealDishMPJLambdaWrapper);
            for (Object object : objects) {
                if (((Integer)object).equals(StatusConstant.DISABLE)){
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
            setmealMapper.updateById(setmeal);
        }
    }

    @Override
    public List<Setmeal> getListByCategoryId(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId())
                        .eq(Setmeal::getStatus,setmeal.getStatus());
        List<Setmeal> setmeals = setmealMapper.selectList(setmealLambdaQueryWrapper);
        return setmeals;
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        MPJLambdaWrapper<SetmealDish> setmealDishMPJLambdaWrapper=new MPJLambdaWrapper<>();
        setmealDishMPJLambdaWrapper.select(Dish::getName)
                .select(SetmealDish::getCopies)
                .select(Dish::getImage)
                .select(Dish::getDescription)
                .eq(SetmealDish::getSetmealId,id)
                .leftJoin(Dish.class,Dish::getId,SetmealDish::getDishId);
        List<DishItemVO> dishItemVOS = setmealDishMapper.selectJoinList(DishItemVO.class, setmealDishMPJLambdaWrapper);
        return dishItemVOS;
    }
}
