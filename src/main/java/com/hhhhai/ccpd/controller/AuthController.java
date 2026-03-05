package com.hhhhai.ccpd.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.dto.LoginDTO;
import com.hhhhai.ccpd.dto.RegisterDTO;
import com.hhhhai.ccpd.service.AuthService;
import com.hhhhai.ccpd.vo.LoginVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制器
 */
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

  @Resource
  private AuthService authService;

  @PostMapping("/login")
  public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
    // todo 参数验证
    LoginVO vo = authService.login(dto);
    return Result.success(vo);
  }

  /**
   * 用户注册
   */
  @PostMapping("/register")
  public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
    // todo 参数验证
    authService.register(dto);
    return Result.success();
  }
}
