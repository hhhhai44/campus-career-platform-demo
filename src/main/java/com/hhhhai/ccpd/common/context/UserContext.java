package com.hhhhai.ccpd.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContext {

  private Long userId;
  private String username;
  private String role;
  // private Long schoolId;   // 为你未来多院校留的
}

