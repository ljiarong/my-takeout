package com.sky.aspect;/**
 * ClassName: AutoFillAspect
 * Package: com.sky.aspect
 */

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * @program: my-takeout
 *
 * @description: 切面类(公共字段自动填充)
 *
 * @author: ljr
 *
 * @create: 2023-10-05 14:09
 **/
@Aspect
//@Component     //仅作为练习，项目中的公共字段填充使用mp来实现
@Slf4j
public class AutoFillAspect {
    @Pointcut(value = "execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void pointCut1(){};

    @Before("pointCut1()")  //前置通知
    public void autoFill(JoinPoint joinPoint){

        log.info("AutoFillAspect的autoFill方法执行中，参数为{}");
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();
        Object[] args = joinPoint.getArgs();
        if(args==null || args.length==0){
            return;
        }
        Object entity = args[0];
        LocalDateTime currentTime=LocalDateTime.now();
        Long user=BaseContext.getCurrentId();
        if (operationType== OperationType.INSERT) {
            try {
                Method setCreateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method setCreateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
                Method setUpdateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);



                setCreateTime.invoke(entity,currentTime);
                setCreateUser.invoke(entity,user);
                setUpdateTime.invoke(entity,currentTime);
                setUpdateUser.invoke(entity,user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (operationType==OperationType.UPDATE)
        {
            try {
                Method setUpdateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);



                setUpdateTime.invoke(entity,currentTime);
                setUpdateUser.invoke(entity,user);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
