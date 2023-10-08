package com.sky.controller.user;/**
 * ClassName: ShoppingCartController
 * Package: com.sky.controller.user
 */

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
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
 * @create: 2023-10-08 16:58
 **/
@RestController
@RequestMapping("user/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @PostMapping("add")
    public Result addShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("ShoppingCartController的addShoppingCart方法执行中，参数为{}",shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }
    @GetMapping("list")
    public Result<List<ShoppingCart>> getShoppingCartList(){
        log.info("ShoppingCartController的getShoppingCartList方法执行中，参数为{}");
        List<ShoppingCart> shoppingCarts= shoppingCartService.getShoppingCartList();
        return Result.success(shoppingCarts);
    }
    @DeleteMapping("clean")
    public Result deleteShoppingCart(){
        log.info("ShoppingCartController的deleteShoppingCart方法执行中，参数为{}");
        shoppingCartService.deleteShoppingCart();
        return Result.success();
    }
    @PostMapping("sub")
    public Result updateShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("ShoppingCartController的updateShoppingCart方法执行中，参数为{}",shoppingCartDTO);
        shoppingCartService.updateShoppingCart(shoppingCartDTO);
        return Result.success();
    }
}
