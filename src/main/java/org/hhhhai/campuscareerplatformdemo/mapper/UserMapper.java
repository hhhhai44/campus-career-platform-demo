package org.hhhhai.campuscareerplatformdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hhhhai.campuscareerplatformdemo.entity.user.UserEntity;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

}

