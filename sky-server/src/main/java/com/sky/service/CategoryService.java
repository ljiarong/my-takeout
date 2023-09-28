package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

/**
 * ClassName: CategoryService
 * Package: com.sky.service
 */
public interface CategoryService {
    void addCategory(CategoryDTO categoryDTO);

    PageResult getCategoryPage(CategoryPageQueryDTO categoryPageQueryDTO);

    void deleteById(Long id);

    void updateCategory(CategoryDTO categoryDTO);

    void updateStatus(Long id, Integer status);

    List<Category> getCategoryList(Integer type);
}
