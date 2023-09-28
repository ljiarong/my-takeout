package com.sky.controller.admin;/**
 * ClassName: CategoryController
 * Package: com.sky.controller.admin
 */

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
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
 * @create: 2023-09-28 09:22
 **/
@RestController
@RequestMapping("admin/category")
@Slf4j
public class CategoryController {
    @Autowired
    CategoryService categoryService;
    @PostMapping
    public Result addCategory(@RequestBody CategoryDTO categoryDTO){
        log.info("CategoryController的addCategory方法执行中，参数为{}",categoryDTO);
        categoryService.addCategory(categoryDTO);
        return Result.success();
    }

    @GetMapping("page")
    public Result<PageResult> getCategoryPage(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("CategoryController的getCategoryPage方法执行中，参数为{}",categoryPageQueryDTO);
        PageResult pageResult=categoryService.getCategoryPage(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    public Result deleteCategoryById(Long id){
        log.info("CategoryController的deleteCategoryById方法执行中，参数为{}",id);
        categoryService.deleteById(id);
        return Result.success();
    }
    @PutMapping
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO){
        log.info("CategoryController的updateCategory方法执行中，参数为{}",categoryDTO);
        categoryService.updateCategory(categoryDTO);
        return Result.success();
    }

    @PostMapping("status/{status}")
    public Result updateStatus(@PathVariable Integer status,Long id){
        log.info("CategoryController的updateStatus方法执行中，参数为{}",status,id);
        categoryService.updateStatus(id,status);
        return Result.success();
    }

    @GetMapping("list")
    public Result<List<Category>> getCategoryList(Integer type){
        log.info("CategoryController的getCategoryList方法执行中，参数为{}",type);
        List<Category> categories=categoryService.getCategoryList(type);
        return Result.success(categories);
    }




}
