package com.sky.annotation;

/**
 * ClassName: AutoFill
 * Package: com.sky.annotation
 */

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
* @Author: ljr
* @Description: 公共字段自动填充标识
* @DateTime: 2023/10/5
* @Params:
* @Return
*/
@Target(ElementType.METHOD)   //指定注解生效位置
@Retention(RetentionPolicy.RUNTIME)  //运行时生效
public @interface AutoFill {
    OperationType value();   //值含有  insert和update
}
