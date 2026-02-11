package org.hhhhai.campuscareerplatformdemo.entity.user;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("user")
@Data
public class UserEntity {

  private Long Id;

  private String username;

  private String password;

  private Integer Role;

  private Long schoolId;

  private Integer status;

}
