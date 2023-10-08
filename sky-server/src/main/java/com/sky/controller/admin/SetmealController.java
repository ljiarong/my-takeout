package com.sky.controller.admin;/**
 * ClassName: SetmealController
 * Package: com.sky.controller.admin
 */

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-10-06 14:26
 **/
@RestController
@RequestMapping("admin/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @PostMapping
    @CacheEvict(key = "'setmeal_'+#setmealDTO.categoryId",value = "Setmeal")
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("SetmealController的addSetmeal方法执行中，参数为{}",setmealDTO);
        setmealService.addSetmeal(setmealDTO);
        return Result.success();
    }

    @GetMapping("page")
    public Result<PageResult> getSetmealPage(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("SetmealController的getSetmealPage方法执行中，参数为{}",setmealPageQueryDTO);
        PageResult pageResult=setmealService.getSetmealPage(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    @CacheEvict(value = "Setmeal",allEntries = true)
    public Result deleteSetmeal(@RequestParam List<Long> ids){
        log.info("SetmealController的deleteSetmeal方法执行中，参数为{}",ids);
        setmealService.deleteSetmeal(ids);
        return Result.success();
    }
    @GetMapping("{id}")
    public Result<SetmealVO> getSetmealById(@PathVariable(value = "id") Long id){
        log.info("SetmealController的getSetmealById方法执行中，参数为{}",id);
        SetmealVO setmealVO=setmealService.getSetmealById(id);
        return Result.success(setmealVO);
    }
    @PutMapping
    @CacheEvict(value = "Setmeal",allEntries = true)
    public Result updateSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("SetmealController的updateSetmeal方法执行中，参数为{}",setmealDTO);
        setmealService.updateSetmeal(setmealDTO);
        return Result.success();
    }
    @PostMapping("status/{status}")
    @CacheEvict(value = "Setmeal",allEntries = true)
    public Result updateStatus(@PathVariable(value = "status") Integer status,@RequestParam Long id){
        log.info("SetmealController的updateStatus方法执行中，参数为{}",id,status);
        setmealService.updateStatus(id,status);
        return Result.success();
    }
}
