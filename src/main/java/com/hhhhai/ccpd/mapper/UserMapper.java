package com.hhhhai.ccpd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.hhhhai.ccpd.entity.user.UserEntity;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

}

