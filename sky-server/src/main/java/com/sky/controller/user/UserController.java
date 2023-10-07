package com.sky.controller.user;/**
 * ClassName: UserController
 * Package: com.sky.controller.user
 */

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-10-07 19:45
 **/
@RestController
@RequestMapping("user/user")
@Slf4j
public class UserController {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserService userService;
    @PostMapping("login")
    public Result<UserLoginVO> userLogin(@RequestBody UserLoginDTO userLoginDTO) throws Exception{
        log.info("UserController的userLogin方法执行中，参数为{}",userLoginDTO);
        User user =userService.login(userLoginDTO);
        Map<String,Object> claims=new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);
        UserLoginVO vo = UserLoginVO.builder().id(user.getId()).openid(user.getOpenid()).token(token).build();
        //TODO:我觉得这里返回token就可以了，但是他文档将id和openid一起返回了，我也就这样写了
        return Result.success(vo);
    }
}
