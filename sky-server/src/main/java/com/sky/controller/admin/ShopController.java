package com.sky.controller.admin;/**
 * ClassName: ShopController
 * Package: com.sky.controller.admin
 */

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-10-06 20:10
 **/
@RequestMapping("admin/shop")
@RestController
@Slf4j
public class ShopController {
    public static final String KEY="SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;
    @PutMapping("{status}")
    public Result updateShopStatus(@PathVariable(value = "status") Integer status){
        log.info("ShopController的updateShopStatus方法执行中，参数为{}",status);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(KEY,status);
        return Result.success();
    }
    @GetMapping("status")
    public Result<Integer> getShopStatus(){
        log.info("ShopController的getShopStatus方法执行中，参数为{}");
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object o = valueOperations.get(KEY);
        return Result.success((Integer) o);
    }
}
