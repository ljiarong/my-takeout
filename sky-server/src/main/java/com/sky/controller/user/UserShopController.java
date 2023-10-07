package com.sky.controller.user;/**
 * ClassName: UserController
 * Package: com.sky.controller.user
 */

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-10-06 20:17
 **/
@RestController
@RequestMapping("user/shop")
@Slf4j
public class UserShopController {
    public static final String KEY="SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("status")
    public Result<Integer> getShopStatus(){
        log.info("UserController的getShopStatus方法执行中，参数为{}");
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        return Result.success(status);
    }
}
