package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * ClassName: UserMapper
 * Package: com.sky.mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    Integer countByMap(Map map);
}
