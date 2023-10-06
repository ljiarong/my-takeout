package com.sky.controller.admin;/**
 * ClassName: DishController
 * Package: com.sky.controller.admin
 */

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RestController
@RequestMapping("admin/dish")
@Slf4j
public class DishController {
    @Autowired
    DishService dishService;
    @PostMapping
    public Result addDish(@RequestBody DishDTO dishDTO){
        log.info("DishController的addDish方法执行中，参数为{}",dishDTO);
        dishService.addDish(dishDTO);
        return Result.success();
    }
    @GetMapping("page")
    public Result<PageResult> getDishPage(DishPageQueryDTO dishPageQueryDTO){
        log.info("DishController的getDishPage方法执行中，参数为{}",dishPageQueryDTO);
        PageResult pageResult=dishService.getDishPage(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    public Result deleteDishById(@RequestParam List<Long> ids){
        log.info("DishController的deleteDishById方法执行中，参数为{}",ids);
        dishService.deleteDishById(ids);
        return Result.success();
    }
    @GetMapping("{id}")
    public Result<DishVO> getDishByid(@PathVariable(name = "id") Long id){
        log.info("DishController的getDishByid方法执行中，参数为{}",id);
        DishVO dishVO=dishService.getDishByid(id);
        return Result.success(dishVO);
    }

    @PutMapping
    public Result updateDish(@RequestBody DishDTO dishDTO){
        log.info("DishController的updateDish方法执行中，参数为{}",dishDTO);
        dishService.updateDish(dishDTO);
        return Result.success();
    }

    @PostMapping("status/{status}")
    public Result updateState(@PathVariable(name = "status") Integer status,
                              @RequestParam Long id){
        log.info("DishController的updateState方法执行中，参数为{}",status,id);
        dishService.updateState(id,status);
        return Result.success();
    }

    @GetMapping("list")
    public Result<List<Dish>> getDishByCategoryId(@RequestParam Long categoryId){
        log.info("DishController的getDishByCategoryId方法执行中，参数为{}",categoryId);
        List<Dish> dishList=dishService.getDishByCategoryId(categoryId);
        return Result.success(dishList);
    }
}
