package com.hhhhai.ccpd.vo.user;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserAdminDetailVO {

  private Long id;

  private String username;

  private String email;

  private String phone;

  private Integer role;

  private String roleDesc;

  private Integer status;

  private String statusDesc;

  private Integer loginFailCount;

  private LocalDateTime lastLoginTime;
}

