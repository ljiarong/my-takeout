package com.sky.service.impl;/**
 * ClassName: UserServiceImpl
 * Package: com.sky.service.impl
 */

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-10-07 20:33
 **/
@Service
public class UserServiceImpl implements UserService {
    public static final String URL="https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String openId = getOpenId(userLoginDTO.getCode());
        if (openId==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断当前用户是否为新用户
        LambdaQueryWrapper<User> userLambdaQueryWrapper=new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getOpenid,openId);
        User user=userMapper.selectOne(userLambdaQueryWrapper);
        if (user==null){
            User user1=User.builder().openid(openId).createTime(LocalDateTime.now()).build();
            userMapper.insert(user1);
            return user1;
        }
        else {
            return user;
        }
    }
    private String getOpenId(String code){
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/sns/jscode2session");
//        uriBuilder.addParameter("appid",weChatProperties.getAppid());
//        uriBuilder.addParameter("secret",weChatProperties.getSecret());
//        uriBuilder.addParameter("js_code",userLoginDTO.getCode());
//        uriBuilder.addParameter("grant_type","authorization_code");
//        HttpGet httpGet = new HttpGet(uriBuilder.build());
//        CloseableHttpResponse response = httpClient.execute(httpGet);
//        int statusCode = response.getStatusLine().getStatusCode();
//        log.info("服务端返回的状态码为：" + statusCode);
//        HttpEntity entity = response.getEntity();
        Map<String,String> map=new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String s = HttpClientUtil.doGet(URL, map);
        JSONObject ob = JSONObject.parseObject(s);
        String openid = ob.getString("openid");
        return openid;
    }
}
