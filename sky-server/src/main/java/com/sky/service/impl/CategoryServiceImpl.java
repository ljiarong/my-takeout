package com.sky.service.impl;/**
 * ClassName: CategoryServiceImpl
 * Package: com.sky.service.impl
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.handler.GlobalExceptionHandler;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-09-28 09:31
 **/
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    DishMapper dishMapper;
    @Autowired
    SetmealMapper setmealMapper;
    @Override
    public void addCategory(CategoryDTO categoryDTO) {
        Category category=new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setStatus(StatusConstant.DISABLE);
        LocalDateTime time=LocalDateTime.now();
        category.setCreateTime(time);
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateTime(time);
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.insert(category);
    }

    @Override
    public PageResult getCategoryPage(CategoryPageQueryDTO categoryPageQueryDTO) {
        IPage<Category> categoryIPage=new Page<>(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());
        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper=new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.like(StringUtils.isNotBlank(categoryPageQueryDTO.getName()),Category::getName,categoryPageQueryDTO.getName())
                .eq(categoryPageQueryDTO.getType()!=null,Category::getType, categoryPageQueryDTO.getType()).orderByAsc(Category::getSort);
        IPage<Category> page = categoryMapper.selectPage(categoryIPage, categoryLambdaQueryWrapper);
        PageResult pageResult = new PageResult();
        pageResult.setRecords(page.getRecords());
        pageResult.setTotal(page.getTotal());
        return pageResult;
    }

    @Override
    public void deleteById(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        Long dishCount = dishMapper.selectCount(dishLambdaQueryWrapper);
        if (dishCount>0){
            throw new DeletionNotAllowedException("存在关联菜品，删除失败");
        }
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        Long setmealCount = setmealMapper.selectCount(setmealLambdaQueryWrapper);
        if(setmealCount>0){
            throw new DeletionNotAllowedException("存在关联套餐，删除失败");
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public void updateCategory(CategoryDTO categoryDTO) {
        Category category=new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.updateById(category);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Category category = Category.builder().id(id).status(status).build();
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.updateById(category);
    }

    @Override
    public List<Category> getCategoryList(Integer type) {
        if (type!=null){
            LambdaQueryWrapper<Category> categoryLambdaQueryWrapper=new LambdaQueryWrapper<>();
            categoryLambdaQueryWrapper.eq(Category::getType,type);
            return categoryMapper.selectList(categoryLambdaQueryWrapper);
        }
        else {
            return categoryMapper.selectList(null);
        }
    }
}
