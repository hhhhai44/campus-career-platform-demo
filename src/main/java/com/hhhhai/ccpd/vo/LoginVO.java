package com.hhhhai.ccpd.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginVO {

  private String token;

  private Long expiresIn;

}
