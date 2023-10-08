package com.sky.handler;/**
 * ClassName: MyMetaObjectHandler
 * Package: com.sky.handler
 */

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @program: my-takeout
 *
 * @description: 公共字段自动填充
 *
 * @author: ljr
 *
 * @create: 2023-10-05 15:52
 **/
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime time= LocalDateTime.now();
        Long userId= BaseContext.getCurrentId();
        if (metaObject.hasSetter(AutoFillConstant.UPDATE_TIME))
        {
            metaObject.setValue(AutoFillConstant.UPDATE_TIME, time);
        }
        if (metaObject.hasSetter(AutoFillConstant.UPDATE_USER)) {
            metaObject.setValue(AutoFillConstant.UPDATE_USER, userId);
        }
        if(metaObject.hasSetter(AutoFillConstant.CREATE_TIME)) {
            metaObject.setValue(AutoFillConstant.CREATE_TIME, time);
        }
        if (metaObject.hasSetter(AutoFillConstant.CREATE_USER)) {
            metaObject.setValue(AutoFillConstant.CREATE_USER, userId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime time= LocalDateTime.now();
        Long userId= BaseContext.getCurrentId();
        if (metaObject.hasSetter(AutoFillConstant.UPDATE_TIME))
        {
            metaObject.setValue(AutoFillConstant.UPDATE_TIME, time);
        }
        if (metaObject.hasSetter(AutoFillConstant.UPDATE_USER)) {
            metaObject.setValue(AutoFillConstant.UPDATE_USER, userId);
        }
    }
}
